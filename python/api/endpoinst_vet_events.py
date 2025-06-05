import json

from django.views.decorators.http import require_http_methods

from api.models import User, Session, VetEvent
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt

SESSION_TOKEN_HEADER = 'SessionToken'

@csrf_exempt
def vet_events(request):
    session_token = request.headers.get('SessionToken')
    if not session_token:
        return JsonResponse({'error': 'Missing token in header'}, status=403)

    try:
        session = Session.objects.get(token=session_token)
    except Session.DoesNotExist:
        return JsonResponse({'error': 'Invalid Token'}, status=403)

    user = session.user

    if request.method == 'GET':
        date_filter = request.GET.get('date')
        if date_filter:
            events = VetEvent.objects.filter(user=user, date=date_filter)
        else:
            events = VetEvent.objects.filter(user=user)

        return JsonResponse([
            {
                'id': e.id,
                'title': e.title,
                'description': e.description,
                'date': e.date.isoformat(),
                'type': e.type
            } for e in events
        ], safe=False)

    elif request.method == 'POST':
        try:
            data = json.loads(request.body)

            event_type = data.get('type')
            event_date = data.get('date')
            description = data.get('description', '')

            if event_type not in dict(VetEvent.EVENT_TYPES):
                return JsonResponse({'error': f'Invalid event type: {event_type}'}, status=400)

            if not event_date:
                return JsonResponse({'error': 'The date field is missing'}, status=400)

            VetEvent.objects.create(
                user=user,
                title=event_type.capitalize(),
                type=event_type,
                date=event_date,
                description=description
            )
            return JsonResponse({'message': 'Event created successfully'}, status=201)

        except (json.JSONDecodeError, KeyError) as e:
            return JsonResponse({'error': f'Error in JSON body: {str(e)}'}, status=400)

    else:
        return JsonResponse({'error': 'HTTP method not supported'}, status=405)


@csrf_exempt
def vet_event_dates(request):
    session_token = request.headers.get('SessionToken')
    if not session_token:
        return JsonResponse({'error': 'Missing token in header'}, status=403)

    try:
        session = Session.objects.get(token=session_token)
    except Session.DoesNotExist:
        return JsonResponse({'error': 'Invalid Token'}, status=403)

    user = session.user

    if request.method == 'GET':
        # Obtiene fechas únicas donde el usuario tiene eventos
        dates = VetEvent.objects.filter(user=user).values_list('date', flat=True).distinct()
        dates_str = [date.isoformat() for date in dates]
        return JsonResponse(dates_str, safe=False)

    else:
        return JsonResponse({'error': 'HTTP method not supported'}, status=405)


@csrf_exempt
def delete_vet_event(request, event_id):
    session_token = request.headers.get('SessionToken')
    if not session_token:
        return JsonResponse({'error': 'Missing token in header'}, status=403)

    try:
        session = Session.objects.get(token=session_token)
    except Session.DoesNotExist:
        return JsonResponse({'error': 'Invalid Token'}, status=403)

    user = session.user

    if request.method == 'DELETE':
        try:
            event = VetEvent.objects.get(id=event_id, user=user)
            event.delete()
            return JsonResponse({'message': 'Event deleted'}, status=200)
        except VetEvent.DoesNotExist:
            return JsonResponse({'error': 'Event not found'}, status=404)
    else:
        return JsonResponse({'error': 'HTTP method not supported'}, status=405)