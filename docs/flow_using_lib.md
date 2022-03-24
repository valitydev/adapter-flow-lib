### Как использовать бибилиотеку?

1. Необходимо провести аналитику и определить какой процесс вам подходит. Список процессов:
    * [Процесс с полным прохождением 3дс версии 1.0 и 2.0](./flows/full_three_ds_v1_v2_flow_steps.md)
    * [Простой процесс с редиректом и поллингом статусов после всех операций](./flows/simple_redirect_with_polling_flow_steps.md)
   
2. Если подходящий вам процесс найден, необходимо выполнить следующие шаги:
    1. Подготавливается конфигурация под spring-boot [настройка конфигурации](./spring-boot-configuration.md).
    2. Релизовать, соответствующие выбранному флоу, методы
       из [RemoteClient](../src/main/java/dev/vality/adapter/flow/lib/client/RemoteClient.java) [инструкция](./client_implementations_manual.md)
    3. Реализовать валидаторы для ваших входных параметров, уникальных для вашего адаптера и настраевымых командой
       поддержки поддержкой(options - на уровне протокола
       damsel) [интерфейс](../src/main/java/dev/vality/adapter/flow/lib/validator/Validator.java)
    4. Реализация тестов

3. Если подходящего процесса нет, необходимо выполнить следующие шаги:
   1. Имплементировать интерфейсы RecurrentResultIntentResolver, ResultIntentResolver, StepResolver (пример: simple-flow)
   2. Далее все как в пункте <b>2</b>
