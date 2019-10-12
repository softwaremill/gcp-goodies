# --- !Ups

CREATE TABLE roomdata (
    id int NOT NULL AUTO_INCREMENT,
    temperature DECIMAL NOT NULL,
    humidity DECIMAL NOT NULL,
    light DECIMAL NOT NULL,
    co2 DECIMAL NOT NULL,
    humidity_ratio DECIMAL NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

drop table  if exists roomdata;
