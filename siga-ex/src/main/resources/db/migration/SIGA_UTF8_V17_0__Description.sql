ALTER TABLE "SIGA"."EX_EMAIL_NOTIFICACAO" ADD (ID_PESSOA NUMBER(10,0), ID_LOTA_EMAIL NUMBER(10,0), ID_PESSOA_EMAIL NUMBER(10,0));
ALTER TABLE "SIGA"."EX_EMAIL_NOTIFICACAO" MODIFY ("ID_LOTACAO" NULL);