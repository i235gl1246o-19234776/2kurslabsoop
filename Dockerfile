FROM tomcat:10.1-jdk17
COPY target/yourapp.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]