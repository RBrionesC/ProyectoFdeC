import json
from datetime import datetime

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .models import Session
from django.conf import settings

SESSION_TOKEN_HEADER = 'SessionToken'


@csrf_exempt
def profile(request):
    session_token = request.headers.get(SESSION_TOKEN_HEADER)
    if not session_token:
        return JsonResponse({'error': 'Missing SessionToken'}, status=403)

    try:
        session = Session.objects.get(token=session_token)
        user = session.user
    except Session.DoesNotExist:
        return JsonResponse({'error': 'Invalid SessionToken'}, status=403)
    if request.method == 'GET':
        avatar_url = request.build_absolute_uri(user.avatar.url) if user.avatar else ""
        if avatar_url and not avatar_url.startswith("http"):
            avatar_url = request.build_absolute_uri(avatar_url)

        return JsonResponse({
            'name': user.name,
            'avatar': avatar_url,
            'dog_name': user.dog_name or "",
            'birthdate': user.birthdate.isoformat() if user.birthdate else "",
            'breed': user.breed or "",
            'weight': user.weight or "",
        }, status=200)


    elif request.method == 'POST':
        try:
            data = json.loads(request.body)

            user.dog_name = data.get('dog_name', user.dog_name)

            user.breed = data.get('breed', user.breed)
            user.weight = data.get('weight', user.weight)

            birthdate_str = data.get('birthdate')
            if birthdate_str:
                try:
                    user.birthdate = datetime.strptime(birthdate_str, "%Y-%m-%d").date()
                except ValueError:
                    return JsonResponse({'error': 'Invalid date. Use the format YYYY-MM-DD.'}, status=400)

            user.save()

            return JsonResponse({'message': 'Profile updated successfully'}, status=200)
        except Exception as e:
            return JsonResponse({'error': 'Error updating profile', 'details': str(e)}, status=400)


    return JsonResponse({'error': 'Method not allowed'}, status=405)
