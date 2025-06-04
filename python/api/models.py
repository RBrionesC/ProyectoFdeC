import bcrypt
from django.db import models

class User(models.Model):
    name = models.CharField(max_length=100)
    avatar = models.ImageField(upload_to="avatars/", null=True, blank=True)
    username = models.CharField(max_length=100)
    encrypted = models.BooleanField(default=False)
    password = models.CharField(max_length=100)
    dog_name = models.CharField(max_length=100, blank=True)
    birthdate = models.DateField(blank=True, null=True)
    breed = models.CharField(max_length=100, blank=True)
    weight = models.CharField(blank=True, null=True)

    def __str__(self):
        return self.username

    def set_password(self, raw_password):
        hashed = bcrypt.hashpw(raw_password.encode(), bcrypt.gensalt())
        self.password = hashed.decode()
        self.save()

    def check_password(self, raw_password):
        if not self.password:
            return False
        return bcrypt.checkpw(raw_password.encode(), self.password.encode())


class Session(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    token = models.CharField(max_length=100)

    def __str__(self):
        return str(self.user) + ' - ' + self.token

class VetEvent(models.Model):
    EVENT_TYPES = [
        ('vaccination', 'Vaccination'),
        ('deworming', 'Deworming'),
        ('visit', 'Visit to the vet'),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE)
    title = models.CharField(max_length=100)
    description = models.TextField(blank=True)
    date = models.DateField()
    type = models.CharField(max_length=20, choices=EVENT_TYPES)

    def __str__(self):
        return f"{self.user.username} - {self.title} ({self.date})"
