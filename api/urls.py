from django.urls import path
from . import views

urlpatterns = [
    path('', views.health, name='home'),
    path('health/', views.health, name='health'),
    path('app-version/', views.app_version, name='app_version'),
    # Auth
    path('api/register/', views.register_user, name='register'),
    path('api/login/', views.login_user, name='login'),

    # Customers
    path('api/add_customer/', views.add_customer, name='add_customer'),
    path('api/get_customers/', views.get_customers, name='get_customers'),
    path('api/customer/<str:mobile>/', views.get_customer_details, name='get_customer_details'),
    path('api/delete_customer/<str:mobile>/', views.delete_customer, name='delete_customer'),

    # Measurements
    path('api/add_measurement/', views.add_measurement, name='add_measurement'),
    path('api/measurements/<str:mobile>/', views.get_measurements, name='get_measurements'),

    # Orders
    path('api/orders/recent/', views.get_recent_orders, name='get_recent_orders'),
    path('api/orders/<int:order_id>/', views.get_order_status, name='get_order_status'),
    path('api/dashboard/stats/', views.get_dashboard_stats, name='get_dashboard_stats'),
    path('api/update_order_status/', views.update_order_status, name='update_order_status'),
    path('api/check_update/', views.check_update, name='check_update'),
    path('api/export-excel/', views.export_customers_excel, name='export-excel'),
    path('api/backup-data/', views.backup_data, name='backup-data'),
]
