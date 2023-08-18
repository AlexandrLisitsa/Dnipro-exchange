Put the Rate-parser.jar, libtdjni.so, Dockerfile in one folder

Execute the following commands:

Run docker build -t dnipro-exchange . Once the image is built, execute the following command to start it:
docker run -d -p 8080:8080 dnipro-exchange Please note that the Telegram (TG) client requires authentication. Currently,
I have linked it to my phone number. The program will prompt for authentication. To authenticate, you need to make a
request to the endpoint:
http://localhost:8080/authorization?code=[code]
