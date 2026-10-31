CREATE DATABASE IF NOT EXISTS ESTACAO_PET;
USE ESTACAO_PET;

CREATE TABLE usuarios(
id_usuarios int auto_increment primary key,
nomeUsuario varchar(200) not null,
loginUsuario varchar(50) not null unique,
senhaHashUsuario varchar(255) not null,
perfilUsuario enum('ADMIN','ATENDENTE') not null default 'ATENDENTE'
);

CREATE TABLE tutor(
id_tutor int auto_increment primary key,
nomeTutor varchar(200) not null,
cpfCifTutor varbinary(255) not null,
cpfHashTutor char(64) not null unique,
telefoneCifTutor varbinary(255) null,
emailCifTutor varbinary(255) null,
enderecoCifTutor varbinary(512) null
);

CREATE TABLE animais(
id_animais int auto_increment primary key,
nomeAnimal varchar(200) not null,
especieAnimal varchar(150) not null,
racaAnimal varchar(100) not null,
idadeAnimal varchar(150) not null,
id_tutor int not null,
foreign key (id_tutor) references tutor(id_tutor)
);

CREATE TABLE agendamentos(
id_agendamentos int auto_increment primary key,
servicosAgendamento enum('banho' , 'tosa' , 'ambos') not null,
dataHoraAgendamento datetime not null unique,
statusAgendamento enum ('agendado' , 'concluido' , 'cancelado' , 'em andamento') not null,
id_animais int not null,
foreign key (id_animais) references animais(id_animais)
);

CREATE TABLE produtos(
idProduto int auto_increment primary key,
nomeProduto varchar(200) not null,
precoProduto decimal(8,2) not null,
estoqueProduto int not null
);

