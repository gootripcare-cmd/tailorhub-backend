from django.urls import path
from . import views

urlpatterns = [
    # Auth
    path('api/register/', views.register_user, name='register'),
    path('api/login/', views.login_user, name='login'),

    # Customers
    path('api/add_customer/', views.add_customer, name='add_customer'),
    path('api/get_customers/', views.get_customers, name='get_customers'),
    path('api/delete_customer/<str:mobile>/', views.delete_customer, name='delete_customer'),

    # Measurements
    path('api/add_measurement/', views.add_measurement, name='add_measurement'),
    path('api/measurements/<str:mobile>/', views.get_measurements, name='get_measurements'),

    # Orders
    path('api/orders/recent/', views.get_recent_orders, name='get_recent_orders'),
    path('api/orders/<int:order_id>/', views.get_order_status, name='get_order_status'),
    path('api/dashboard/stats/', views.get_dashboard_stats, name='get_dashboard_stats'),
]
