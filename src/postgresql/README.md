##  Preparation for postgres on localhost

### Local up postgres
- установить утилиты psql и docker/docker-compose
- поднимаем контейнер с базой данных
    - **docker-compose up**
- узнаем ip контейнера
    - **docker inspect postgresql | grep "IPAddress"**
- подставляем ip в конфигурацию spring.datasource.url
- запускаем приложение и все готово

### Local connect with psql
- psql -h container-ip -p 5432 -U user db

Таким образом попадаем внутрь контейнера в cli базы данных
