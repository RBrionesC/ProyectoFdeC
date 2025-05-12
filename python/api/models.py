from django.db import models

class User(models.Model):
    name = models.CharField(max_length=100)
    username = models.CharField(max_length=100)
    encrypted = models.BooleanField(default=False)
    password = models.CharField(max_length=100)


    def __str__(self):
        return self.username

class Session(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    token = models.CharField(max_length=100)

    def __str__(self):
        return str(self.user) + ' - ' + self.token
