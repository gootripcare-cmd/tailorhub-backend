from django.db import models


class TailorUser(models.Model):
    """Mirrors the 'users' table in the Android SQLite DatabaseHelper."""
    first_name = models.CharField(max_length=100, blank=True, default='')
    last_name = models.CharField(max_length=100, blank=True, default='')
    mobile_number = models.CharField(max_length=20, unique=True)
    username = models.CharField(max_length=150, unique=True)
    password = models.CharField(max_length=255)  # plain-text for simplicity (hash in production)

    class Meta:
        db_table = 'users'

    def __str__(self):
        return f"{self.first_name} {self.last_name} ({self.username})"


class Customer(models.Model):
    """Mirrors the 'tailorhub_customer' table in the Android SQLite DatabaseHelper."""
    name = models.CharField(max_length=200)
    mobile_number = models.CharField(max_length=20, unique=True)
    address = models.TextField(blank=True, default='')
    length = models.CharField(max_length=50, blank=True, default='')

    class Meta:
        db_table = 'tailorhub_customer'

    def __str__(self):
        return f"{self.name} ({self.mobile_number})"


class Measurement(models.Model):
    """Mirrors the 'tailorhub_measurements' table in the Android SQLite DatabaseHelper."""
    GARMENT_CHOICES = [
        ('Shirt', 'Shirt'),
        ('Pant', 'Pant'),
        ('Koti', 'Koti'),
        ('Suit', 'Suit'),
        ('Jabbho', 'Jabbho'),
        ('Lehngho', 'Lehngho'),
        ('Safari', 'Safari'),
        ('Jodhpuri', 'Jodhpuri'),
    ]

    STATUS_CHOICES = [
        ('Pending', 'Pending'),
        ('In Progress', 'In Progress'),
        ('Completed', 'Completed'),
        ('Delivered', 'Delivered'),
    ]

    customer = models.ForeignKey(
        Customer,
        on_delete=models.CASCADE,
        related_name='measurements',
        db_column='customer_id',
    )
    garment_type = models.CharField(max_length=50, choices=GARMENT_CHOICES, default='Shirt')
    length = models.CharField(max_length=20, blank=True, default='')
    chest = models.CharField(max_length=20, blank=True, default='')
    waist = models.CharField(max_length=20, blank=True, default='')
    collar = models.CharField(max_length=20, blank=True, default='')
    shoulder = models.CharField(max_length=20, blank=True, default='')
    sleeve = models.CharField(max_length=20, blank=True, default='')
    hip = models.CharField(max_length=20, blank=True, default='')
    rise = models.CharField(max_length=20, blank=True, default='')
    notes = models.TextField(blank=True, default='')
    status = models.CharField(max_length=50, choices=STATUS_CHOICES, default='Pending')

    class Meta:
        db_table = 'tailorhub_measurements'

    def __str__(self):
        return f"{self.garment_type} for {self.customer.name}"


class Order(models.Model):
    """Mirrors the 'tailorhub_order' table in the Android SQLite DatabaseHelper."""
    STATUS_CHOICES = [
        ('Pending', 'Pending'),
        ('In Progress', 'In Progress'),
        ('Completed', 'Completed'),
        ('Delivered', 'Delivered'),
    ]

    customer = models.ForeignKey(
        Customer,
        on_delete=models.CASCADE,
        related_name='orders',
        db_column='customer_id',
    )
    garment_type = models.CharField(max_length=50, default='Shirt')
    status = models.CharField(max_length=50, choices=STATUS_CHOICES, default='Pending')
    order_date = models.CharField(max_length=50, blank=True, default='')
    estimated_completion_date = models.CharField(max_length=50, blank=True, default='')

    class Meta:
        db_table = 'tailorhub_order'

    def __str__(self):
        return f"Order #{self.id} – {self.customer.name} ({self.garment_type})"

class AppConfig(models.Model):
    latest_version_code = models.IntegerField(default=1)
    latest_version_name = models.CharField(max_length=20, default="1.0.0")
    download_url = models.URLField(max_length=500, blank=True, null=True)
    force_update = models.BooleanField(default=False)
    update_message = models.TextField(blank=True, default="A new update is available!")

    def __str__(self):
        return f"v{self.latest_version_name} ({self.latest_version_code})"
