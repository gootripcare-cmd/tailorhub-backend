from rest_framework import serializers
from .models import TailorUser, Customer, Measurement, Order


class TailorUserSerializer(serializers.ModelSerializer):
    class Meta:
        model = TailorUser
        fields = ['id', 'first_name', 'last_name', 'mobile_number', 'username', 'password']
        extra_kwargs = {'password': {'write_only': True}}


class CustomerSerializer(serializers.ModelSerializer):
    status = serializers.SerializerMethodField()

    class Meta:
        model = Customer
        fields = ['id', 'name', 'mobile_number', 'address', 'length', 'status']

    def get_status(self, obj):
        # Get the latest order status for this customer
        latest_order = obj.orders.order_by('-id').first()
        return latest_order.status if latest_order else "No Orders"


class MeasurementSerializer(serializers.ModelSerializer):
    class Meta:
        model = Measurement
        fields = [
            'id', 'customer', 'garment_type',
            'length', 'chest', 'waist', 'collar',
            'shoulder', 'sleeve', 'hip', 'rise',
            'notes', 'status',
        ]


class OrderSerializer(serializers.ModelSerializer):
    class Meta:
        model = Order
        fields = ['id', 'customer', 'garment_type', 'status', 'order_date', 'estimated_completion_date']


class OrderStatusSerializer(serializers.ModelSerializer):
    """Serializer used for the GetOrderStatus endpoint expected by the Android app."""
    customer_name = serializers.SerializerMethodField()

    class Meta:
        model = Order
        fields = ['id', 'customer_name', 'status', 'estimated_completion_date']

    def get_customer_name(self, obj):
        return obj.customer.name
