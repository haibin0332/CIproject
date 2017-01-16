http://lavasoft.blog.51cto.com/62575/723192/


SZV1000178694:~ # su - redis
redis@SZV1000178694:~> redis-cli
redis 127.0.0.1:6379> get task1484547855321:status
"ACCEPTED"
redis 127.0.0.1:6379> get task1484547863445:status
"OK"
redis 127.0.0.1:6379> get task1484547855321:status
"OK"


