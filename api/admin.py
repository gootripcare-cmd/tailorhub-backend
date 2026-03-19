from django.contrib import admin
from .models import TailorUser, Customer, Measurement, Order, AppConfig


@admin.register(TailorUser)
class TailorUserAdmin(admin.ModelAdmin):
    list_display = ['id', 'username', 'first_name', 'last_name', 'mobile_number']
    search_fields = ['username', 'mobile_number']


@admin.register(Customer)
class CustomerAdmin(admin.ModelAdmin):
    list_display = ['id', 'name', 'mobile_number', 'address']
    search_fields = ['name', 'mobile_number']


@admin.register(Measurement)
class MeasurementAdmin(admin.ModelAdmin):
    list_display = ['id', 'customer', 'garment_type', 'length', 'chest', 'status']
    list_filter = ['garment_type', 'status']
    search_fields = ['customer__name', 'customer__mobile_number']


@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ['id', 'customer', 'garment_type', 'status', 'order_date']
    list_filter = ['garment_type', 'status']
    search_fields = ['customer__name', 'customer__mobile_number']


@admin.register(AppConfig)
class AppConfigAdmin(admin.ModelAdmin):
    list_display = ['id', 'latest_version_name', 'latest_version_code', 'force_update', 'download_url']
