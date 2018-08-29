To push the server as a prod web server, we use Nginx + uwsgi + PostgreSQL + LetsEncrypt. This readme is by no means to be comprehensive. I just want to jot down the popular commands that I used, and also provide the templates of the configuration files.

### Frequently used commands
```
#start the uwsgi service
sudo uwsgi --ini notification_uwsgi.ini
sudo uwsgi --reload /tmp/notification.pid
sudo uwsgi --stop /tmp/notification.pid

#nginx commands
sudo service nginx status
sudo service nginx start
sudo service nginx restart
sudo service nginx stop
#check nginx config
sudo service nginx configtest

#If you update css/js
python manage.py collectstatic

#log file
/var/log/uwsgi/notification.log
```


### Template of notification_uwsgi.ini

```
# mysite_uwsgi.ini file
[uwsgi]

# Django-related settings
# the base directory (full path)
chdir           = <absolute/path/to/EngagementService/repo>/Nurture/server/notification/

# Django's wsgi file
module          = notification.wsgi
# the virtualenv (full path)
#home            = <path/to/virtualenv/folder>

# process-related settings
# master
master          = true
# maximum number of worker processes
processes       = 10
# the socket (use the full path to be safe
socket          = <absolute/path/to/some/folder/notification.sock>
# ... with appropriate permissions - may be needed
chmod-socket    = 666
# clear environment on exit
vacuum          = true

# set an environment variable
env = DJANGO_SETTINGS_MODULE=notification.settings

# create a pidfile
safe-pidfile = /tmp/notification.pid

# respawn processes taking more than 20 seconds
harakiri = 20

# limit the project to 3GB
limit-as = 3072

# respawn processes after serving 100 requests
max-requests = 100

# background the process & log
daemonize = /var/log/uwsgi/notification.log
```

### Template of Nginx config file
```
upstream django {
    server unix:///<absolute/path/to/some/folder/notification.sock>; # for a file socket
    #server 127.0.0.1:8001; # for a web port socket (we'll use this first)
}

server {
    if ($host = dijkstra.nesl.ucla.edu) {
        return 301 https://$host$request_uri;
    } # managed by Certbot


	listen      80 default_server;
	listen [::]:80 default_server ipv6only=on;
	server_name <your.domain.name.edu>;
	return 301  https://$host$request_uri;


}

server {
	listen      443 ssl;
	listen [::]:443 ipv6only=on;

	#root /usr/share/nginx/html;
	#index index.html index.htm;

	# Make site accessible from http://localhost/
	server_name <your.domain.name.edu>;
	charset  utf-8;
    ssl_certificate /etc/letsencrypt/live/<your.domain.name.edu>/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/<your.domain.name.edu>/privkey.pem; # managed by Certbot

	include /etc/letsencrypt/options-ssl-nginx.conf;

	location /static {
		alias <absolute/path/to/EngagementService/repo>/Nurture/server/notification/static;
	}

	# For SSL/TLS
	location ~ /.well-known {
		allow all;
	}

	# send all non-media request to django server
	location / {
		uwsgi_pass  django;
		include     uwsgi_params;
	}
}
```
