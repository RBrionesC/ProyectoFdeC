import json
import secrets
import bcrypt
from django.db import IntegrityError
from django.views.decorators.http import require_http_methods

from api.models import User, Session, VetEvent
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

SESSION_TOKEN_HEADER = 'SessionToken'


@csrf_exempt
def register(request):
    if request.method == 'POST':
        try:
            body_json = json.loads(request.body)
            json_name = body_json.get('name', None)
            json_email = body_json.get('email', None)
            json_password = body_json.get('password', None)

            if json_name is None:
                return JsonResponse({"error": "name can't be None"}, status=400)
            if json_email is None:
                return JsonResponse({"error": "email can't be None"}, status=400)
            if json_password is None:
                return JsonResponse({"error": "password can't be None"}, status=400)

            salted_and_hashed_pass = bcrypt.hashpw(json_password.encode('utf8'), bcrypt.gensalt()).decode('utf8')
            user_object = User(name=json_name, username=json_email, password=salted_and_hashed_pass)
            user_object.save()

            # Login automático tras registrarse
            random_token = secrets.token_hex(10)
            session = Session(user=user_object, token=random_token)
            session.save()
            return JsonResponse({"token": random_token}, status=201)
        except IntegrityError:
            return JsonResponse({"error": "username already taken"}, status=409)
        except Exception as e:
            # Agrega esto para ver errores inesperados
            return JsonResponse({"error": str(e)}, status=400)
    elif request.method == 'GET':
        json_response = []
        all_rows = User.objects.all()
        for row in all_rows:
            json_response.append(row.name)
        return JsonResponse(json_response, status=200, safe=False)
    else:
        return JsonResponse({'error': 'Unsupported HTTP method'}, status=405)


@csrf_exempt
def login(request):
    if request.method == 'POST':
        try:
            body_json = json.loads(request.body)
        except json.JSONDecodeError as e:
            return JsonResponse({"error": f"Malformed JSON: {str(e)}"}, status=400)

        json_email = body_json.get('email', None)
        json_password = body_json.get('password', None)

        if json_email is None:
            return JsonResponse({"error": "email can't be None"}, status=400)
        if json_password is None:
            return JsonResponse({"error": "password can't be None"}, status=400)

        try:
            user_object = User.objects.get(username=json_email)
        except User.DoesNotExist:
            return JsonResponse({"error": "User does not exist"}, status=404)


        if bcrypt.checkpw(json_password.encode('utf8'), user_object.password.encode('utf8')):

            # Las contraseñas coinciden, procedemos a iniciar sesión
            random_token = secrets.token_hex(10)
            session = Session(user=user_object, token=random_token)
            session.save()
            return JsonResponse({"token": random_token, "name": user_object.name, "email": user_object.username},
                                status=201)
        else:
            return JsonResponse({"error": "Not valid password"}, status=401)
    elif request.method == 'DELETE':
        json_token = request.headers.get('SessionToken', None)

        if json_token is None:
            return JsonResponse({"error": "token can't be None"}, status=400)

        try:
            session_object = Session.objects.get(token=json_token)
        except Session.DoesNotExist:
            return JsonResponse({"error": "Session does not exist"}, status=404)

        session_object.delete()
        return JsonResponse({"status": "logged out"}, status=200)
    else:
        return JsonResponse({'error': 'Unsupported HTTP method'}, status=405)
