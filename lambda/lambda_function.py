import json
import boto3
from botocore.config import Config

def lambda_handler(event, context):
    print(f"Received event: {json.dumps(event)}")

    try:
        # Parse body
        if isinstance(event.get('body'), str):
            try:
                body = json.loads(event['body'])
            except json.JSONDecodeError as e:
                print(f"Failed to parse body: {event['body']}")
                return {
                    'statusCode': 400,
                    'headers': {
                        'Content-Type': 'application/json',
                        'Access-Control-Allow-Origin': '*'
                    },
                    'body': json.dumps({'error': 'Invalid JSON in request body'})
                }
        else:
            body = event

        print(f"Parsed body: {json.dumps(body)}")

        input_text = body.get('inputText', '')
        print(f"Extracted input text: {input_text}")

        if not input_text:
            return {
                'statusCode': 400,
                'headers': {
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin': '*'
                },
                'body': json.dumps({'error': 'Input text is required'})
            }

        config = Config(
            connect_timeout=5,
            read_timeout=25,
            retries={'max_attempts': 2}
        )

        client = boto3.client('bedrock-runtime',
                            region_name='us-east-1',
                            config=config)

        # Extract parameters with defaults
        model_id = body.get('modelId', 'amazon.titan-text-lite-v1')
        max_token_count = body.get('maxTokenCount', 100)
        temperature = body.get('temperature', 0.4)
        top_p = body.get('topP', 0.9)

        # Simple payload structure
        payload = {
            "inputText": input_text,
            "textGenerationConfig": {
                "maxTokenCount": max_token_count,
                "temperature": temperature,
                "topP": top_p,
                "stopSequences": []
            }
        }

        print(f"Sending payload to Bedrock: {json.dumps(payload)}")

        response = client.invoke_model(
            modelId=model_id,
            contentType="application/json",
            accept="application/json",
            body=json.dumps(payload)
        )

        response_body = json.loads(response['body'].read())
        return {
            'statusCode': 200,
            'headers': {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            'body': json.dumps(response_body)
        }

    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            'body': json.dumps({'error': str(e)})
        }