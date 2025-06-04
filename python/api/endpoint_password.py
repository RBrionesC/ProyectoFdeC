from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
import json
from .models import Session

@csrf_exempt
def change_password(request):
    if request.method != 'POST':
        return JsonResponse({"error": "Method not allowed"}, status=405)

    token = request.headers.get('SessionToken')
    if not token:
        return JsonResponse({"error": "Token not provided"}, status=400)

    try:
        data = json.loads(request.body)
        current_password = data.get('current_password')
        new_password = data.get('new_password')
    except Exception:
        return JsonResponse({"error": "Invalid data"}, status=400)

    if not current_password or not new_password:
        return JsonResponse({"error": "Missing parameters"}, status=400)

    try:
        session = Session.objects.get(token=token)
        user = session.user
    except Session.DoesNotExist:
        return JsonResponse({"error": "Invalid Token"}, status=401)

    if not user.check_password(current_password):
        return JsonResponse({"error": "Current password incorrect"}, status=400)

    user.set_password(new_password)

    return JsonResponse({"message": "Password changed successfully"})
