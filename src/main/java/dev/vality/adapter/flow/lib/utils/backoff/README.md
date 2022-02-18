# Поллинг для адаптеров

## Настройки

Название параметра | Описание | Пример
------------ | ------------- | -------------
**exponential** | экспонента | 2
**max_time_backoff** | максимальное время для ограничения роста экспоненты | 600 (10 минут)
**default_initial_exponential** | экспонента по умолчанию | 2

## Пример использования:

```
int nextPollingInterval = BackOffUtils.prepareNextPollingInterval(adapterState, options);
```

или

```
BackOffExecution backOffExecution = BackOffUtils.prepareBackOffExecution(adapterState, options);
int nextPollingInterval = backOffExecution.nextBackOff().intValue();
```

или

```
ExponentialBackOff exponentialBackOff = new ExponentialBackOff(adapterState, options);
int nextPollingInterval = exponentialBackOff.start().nextBackOff().intValue();
```
