import json
from datetime import date

from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.response import Response

from .models import TailorUser, Customer, Measurement, Order
from .serializers import (
    TailorUserSerializer,
    CustomerSerializer,
    OrderStatusSerializer,
)


# ──────────────────────────── AUTH ────────────────────────────

@api_view(['POST'])
def register_user(request):
    """
    POST /api/register/
    Body: { first_name, last_name, mobile_number, username, password }
    """
    data = request.data

    first_name = data.get('first_name', '').strip()
    last_name = data.get('last_name', '').strip()
    mobile_number = data.get('mobile_number', '').strip()
    username = data.get('username', '').strip()
    password = data.get('password', '').strip()

    if not username or not password or not mobile_number:
        return Response(
            {'error': 'username, password, and mobile_number are required.'},
            status=status.HTTP_400_BAD_REQUEST,
        )

    if TailorUser.objects.filter(username=username).exists():
        return Response(
            {'error': 'Username already taken.'},
            status=status.HTTP_409_CONFLICT,
        )

    if TailorUser.objects.filter(mobile_number=mobile_number).exists():
        return Response(
            {'error': 'Mobile number already registered.'},
            status=status.HTTP_409_CONFLICT,
        )

    TailorUser.objects.create(
        first_name=first_name,
        last_name=last_name,
        mobile_number=mobile_number,
        username=username,
        password=password,
    )
    return Response({'message': 'Registration successful.'}, status=status.HTTP_201_CREATED)


@api_view(['POST'])
def login_user(request):
    """
    POST /api/login/
    Body: { username, password }
    Returns: { status, message, user_id }
    """
    data = request.data
    username = data.get('username', '').strip()
    password = data.get('password', '').strip()

    if not username or not password:
        return Response(
            {'status': 'error', 'error': 'username and password are required.'},
            status=status.HTTP_400_BAD_REQUEST,
        )

    try:
        user = TailorUser.objects.get(username=username, password=password)
        return Response(
            {'status': 'success', 'message': 'Login successful.', 'user_id': user.id},
            status=status.HTTP_200_OK,
        )
    except TailorUser.DoesNotExist:
        return Response(
            {'status': 'error', 'message': 'Invalid credentials.'},
            status=status.HTTP_401_UNAUTHORIZED,
        )


# ──────────────────────────── CUSTOMERS ────────────────────────────

@api_view(['POST'])
def add_customer(request):
    """
    POST /api/add_customer/
    Body: { name, mobile_number, address }
    """
    data = request.data
    name = data.get('name', '').strip()
    mobile_number = data.get('mobile_number', '').strip()
    address = data.get('address', '').strip()

    if not name or not mobile_number:
        return Response(
            {'error': 'name and mobile_number are required.'},
            status=status.HTTP_400_BAD_REQUEST,
        )

    # Upsert: if customer already exists (by mobile), update them
    customer, created = Customer.objects.update_or_create(
        mobile_number=mobile_number,
        defaults={'name': name, 'address': address},
    )

    serializer = CustomerSerializer(customer)
    status_code = status.HTTP_201_CREATED if created else status.HTTP_200_OK
    return Response(serializer.data, status=status_code)


@api_view(['GET'])
def get_customers(request):
    """
    GET /api/get_customers/
    Returns a list of all customers.
    """
    customers = Customer.objects.all().order_by('name')
    serializer = CustomerSerializer(customers, many=True)
    return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(['DELETE'])
def delete_customer(request, mobile):
    """
    DELETE /api/delete_customer/<mobile>/
    """
    try:
        customer = Customer.objects.get(mobile_number=mobile)
        customer.delete()
        return Response({'message': 'Customer deleted.'}, status=status.HTTP_200_OK)
    except Customer.DoesNotExist:
        return Response({'error': 'Customer not found.'}, status=status.HTTP_404_NOT_FOUND)


# ──────────────────────────── MEASUREMENTS ────────────────────────────

@api_view(['POST'])
def add_measurement(request):
    """
    POST /api/add_measurement/
    Body: {
        user_id, mobile_number, garment_type,
        length, chest, waist, collar, shoulder, sleeve,
        notes, status, is_update
    }
    Creates a new Measurement (and a corresponding Order) for the customer
    identified by mobile_number. If is_update == 'true', replaces the latest
    measurement for that garment type instead of inserting a new record.
    """
    data = request.data
    mobile_number = data.get('mobile_number', '').strip()
    garment_type = data.get('garment_type', 'Shirt').strip()
    is_update_str = data.get('is_update', 'false').strip().lower()
    is_update = is_update_str == 'true'

    # Find customer by mobile number
    try:
        customer = Customer.objects.get(mobile_number=mobile_number)
    except Customer.DoesNotExist:
        return Response({'error': 'Customer not found.'}, status=status.HTTP_404_NOT_FOUND)

    measurement_fields = {
        'customer': customer,
        'garment_type': garment_type,
        'length': data.get('length', '').strip(),
        'chest': data.get('chest', '').strip(),
        'waist': data.get('waist', '').strip(),
        'collar': data.get('collar', '').strip(),
        'shoulder': data.get('shoulder', '').strip(),
        'sleeve': data.get('sleeve', '').strip(),
        'notes': data.get('notes', '').strip(),
        'status': data.get('status', 'Pending').strip(),
    }

    if is_update:
        # Update the most recent measurement for this customer + garment_type
        latest = (
            Measurement.objects.filter(customer=customer, garment_type=garment_type)
            .order_by('-id')
            .first()
        )
        if latest:
            for attr, value in measurement_fields.items():
                setattr(latest, attr, value)
            latest.save()
            measurement = latest
        else:
            # No existing record — create instead
            measurement = Measurement.objects.create(**measurement_fields)
    else:
        measurement = Measurement.objects.create(**measurement_fields)

    # Also create / update a corresponding Order record
    today_str = date.today().strftime('%b %d, %Y')
    order_status = measurement_fields['status']

    if is_update:
        existing_order = (
            Order.objects.filter(customer=customer, garment_type=garment_type)
            .order_by('-id')
            .first()
        )
        if existing_order:
            existing_order.status = order_status
            existing_order.save()
        else:
            Order.objects.create(
                customer=customer,
                garment_type=garment_type,
                status=order_status,
                order_date=today_str,
            )
    else:
        Order.objects.create(
            customer=customer,
            garment_type=garment_type,
            status=order_status,
            order_date=today_str,
        )

    return Response({'message': 'Measurement saved.'}, status=status.HTTP_201_CREATED)


@api_view(['GET'])
def get_measurements(request, mobile):
    """
    GET /api/measurements/<mobile>/?garment_type=Shirt
    Returns the customer's most recent measurement for this garment, mapped so Android can parse it.
    """
    garment_type = request.query_params.get('garment_type', 'Shirt')
    
    try:
        customer = Customer.objects.get(mobile_number=mobile)
        measurement = (
            Measurement.objects.filter(customer=customer, garment_type=garment_type)
            .order_by('-id')
            .first()
        )
        
        if not measurement:
            return Response({'error': 'No matching measurements found.'}, status=status.HTTP_404_NOT_FOUND)
            
        return Response({
            'id': measurement.id,
            'garment_type': measurement.garment_type,
            'length': measurement.length,
            'chest': measurement.chest,
            'waist': measurement.waist,
            'collar': measurement.collar,
            'shoulder': measurement.shoulder,
            'sleeve': measurement.sleeve,
            'hip': measurement.hip,
            'rise': measurement.rise,
            'notes': measurement.notes,
            'status': measurement.status
        }, status=status.HTTP_200_OK)
        
    except Customer.DoesNotExist:
        return Response({'error': 'Customer not found.'}, status=status.HTTP_404_NOT_FOUND)

# ──────────────────────────── ORDERS ────────────────────────────

@api_view(['GET'])
def get_order_status(request, order_id):
    """
    GET /api/orders/<orderId>/
    Returns: { id, customer_name, status, estimated_completion_date }
    """
    try:
        order = Order.objects.select_related('customer').get(pk=order_id)
        serializer = OrderStatusSerializer(order)
        return Response(serializer.data, status=status.HTTP_200_OK)
    except Order.DoesNotExist:
        return Response({'error': 'Order not found.'}, status=status.HTTP_404_NOT_FOUND)


@api_view(['GET'])
def get_recent_orders(request):
    """
    GET /api/orders/recent/?limit=5
    Returns the most recent orders with customer name, garment type, status.
    """
    limit = int(request.query_params.get('limit', 5))
    orders = (
        Order.objects.select_related('customer')
        .order_by('-id')[:limit]
    )
    data = [
        {
            'id': str(o.id),
            'customer_name': o.customer.name,
            'garment_type': o.garment_type,
            'status': o.status,
            'order_date': o.order_date,
        }
        for o in orders
    ]
    return Response(data, status=status.HTTP_200_OK)


@api_view(['GET'])
def get_dashboard_stats(request):
    """
    GET /api/dashboard/stats/
    Returns: { total_customers, total_orders, pending_orders, completed_orders }
    """
    try:
        total_customers = Customer.objects.count()
        total_orders = Order.objects.count()
        pending_orders = Order.objects.filter(status='Pending').count()
        completed_orders = Order.objects.filter(status='Completed').count()

        data = {
            'total_customers': total_customers,
            'total_orders': total_orders,
            'pending_orders': pending_orders,
            'completed_orders': completed_orders,
        }
        return Response(data, status=status.HTTP_200_OK)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
