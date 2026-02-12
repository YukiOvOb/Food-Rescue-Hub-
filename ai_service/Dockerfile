FROM python:3.11-slim

WORKDIR /app

COPY ai_service/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ai_service/ ./

EXPOSE 8000

CMD ["python", "api.py"]
