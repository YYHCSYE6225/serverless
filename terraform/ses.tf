resource "aws_sns_topic" "email_verification" {
  name = "email-verification-topic"
}


resource "aws_sns_topic_subscription" "topic_lambda" {
  topic_arn = aws_sns_topic.email_verification.arn
  protocol  = "lambda"
  endpoint  = aws_lambda_function.lambda.arn
}

resource "aws_lambda_function" "lambda" {
  s3_bucket     = "codedeploy.yyh.s3bucket"
  s3_key        = "serverless-1.0-SNAPSHOT.jar"
  function_name = "email_verification"
  role          = "arn:aws:iam::254269847591:role/lambda-role"
  handler       = "com.SendEmail::handleRequest"
  runtime       = "java11"
  timeout       = "20"
  memory_size   = "512"
  # source_code_hash = filebase64sha256("serverless-1.0-SNAPSHOT.jar")
}

resource "aws_lambda_permission" "with_sns" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda.arn
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.email_verification.arn
}