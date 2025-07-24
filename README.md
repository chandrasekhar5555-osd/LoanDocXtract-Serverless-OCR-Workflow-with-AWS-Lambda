# LoanDocXtract: Serverless OCR Workflow with AWS Lambda

Automate document intake and classification using a fully serverless architecture powered by AWS Lambda, S3, and API Gateway. Designed to eliminate manual processing, improve compliance, and accelerate document workflows.

---

## 🚀 Features

- Upload loan documents to S3 and trigger OCR classification automatically
- Serverless architecture using AWS Lambda and API Gateway
- CI/CD integration with GitHub Actions
- Backend written in Java with Spring Boot
- End-to-end test automation using Cypress
- Unit and integration testing with JUnit and Mockito

---

## 📁 Project Structure

```
.
├── src/                      # Java source code for AWS Lambda functions
├── scripts/                 # Deployment and CI/CD scripts
├── tests/                   # Cypress and JUnit test cases
├── .github/workflows/       # GitHub Actions pipelines
├── README.md
└── template.yaml            # AWS SAM/CloudFormation template
```

---

## 🧑‍💻 Author

**Chandra Sekhar Pramod Mahaveer Neelam**  
Java Full Stack Developer
📧 Email: chandrasekharneelam8@gmail.com  
📞 Phone: +1 940-843-3314  
🔗 [LinkedIn](http://www.linkedin.com/in/chandraneelam5)

---

## 📦 How to Fork and Run This Project

### 🔁 Step 1: Fork the Repository

1. Click the `Fork` button on the top right of this page.
2. Clone your forked repository:

```bash
git clone https://github.com/YOUR-USERNAME/LoanDocXtract-Serverless-OCR-Workflow-with-AWS-Lambda.git
cd LoanDocXtract-Serverless-OCR-Workflow-with-AWS-Lambda
```

---

### 🛠️ Step 2: Prerequisites

- Java 17
- AWS CLI configured
- AWS SAM CLI (for local deployment)
- Node.js and npm (for Cypress tests)
- Docker (for local Lambda testing)

---

### ⚙️ Step 3: Run the Project

#### Deploy Locally Using AWS SAM

```bash
sam build
sam deploy --guided
```

#### Run Tests

**JUnit (Java)**

```bash
./gradlew test
```

**Cypress (End-to-End)**

```bash
cd tests
npm install
npx cypress open
```

---

## 🧪 Sample API Instruction

After deployment, use this sample cURL command to test the OCR API:

```bash
curl -X POST https://<your-api-id>.execute-api.<region>.amazonaws.com/prod/ocr   -H "Content-Type: multipart/form-data"   -F "file=@sample-loan-doc.pdf"
```

---

## ✅ Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.
