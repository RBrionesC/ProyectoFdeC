"""
URL configuration for python project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from django.conf import settings
from django.conf.urls.static import static

from api import endpoints, endpoint_upload_image, endpoints_profile, endpoinst_vet_events

urlpatterns = [
    path('admin/', admin.site.urls),
    path('user/', endpoints.register),
    path('sessions/', endpoints.login),
    path('users/upload_avatar/', endpoint_upload_image.upload_avatar),
    path('users/profile/', endpoints_profile.profile),
    path('vetevent/', endpoinst_vet_events.vet_events),
    path('vetevent/dates/', endpoinst_vet_events.vet_event_dates),
    path('vetevent/<int:event_id>/', endpoinst_vet_events.delete_vet_event),


]
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)