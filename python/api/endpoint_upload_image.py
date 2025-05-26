import json, base64
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.core.files.base import ContentFile
from .models import Session, User  # Asegúrate de importar tu modelo de usuario si es personalizado

SESSION_TOKEN_HEADER = 'SessionToken'

# Utilidad para decodificar imagen base64
def base64_file(data, name=None):
    _format, _img_str = data.split(';base64,')
    _name, ext = _format.split('/')
    if not name:
        name = _name.split(":")[-1]
    return ContentFile(base64.b64decode(_img_str), name='{}.{}'.format(name, ext))

@csrf_exempt
def upload_avatar(request):
    if request.method != 'POST':
        return JsonResponse({'error': 'Método HTTP no soportado.'}, status=405)

    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'JSON mal formado.'}, status=400)

    imagen_base64 = body.get('imagen')
    if not imagen_base64:
        return JsonResponse({'error': 'Falta la imagen.'}, status=400)

    session_token = request.headers.get(SESSION_TOKEN_HEADER)
    if not session_token:
        return JsonResponse({'error': 'SessionToken no proporcionado.'}, status=403)

    try:
        session = Session.objects.get(token=session_token)
        user = session.user
    except Session.DoesNotExist:
        return JsonResponse({'error': 'SessionToken inválido.'}, status=403)

    try:
        avatar = base64_file(imagen_base64, name=f'avatar_{user.id}')
    except (ValueError, TypeError, IndexError, base64.binascii.Error):
        return JsonResponse({'error': 'Formato de imagen no válido.'}, status=400)

    # Guardar la imagen en el usuario
    user.avatar = avatar
    user.save()

    return JsonResponse({'success': 'Avatar actualizado correctamente.'}, status=200)
