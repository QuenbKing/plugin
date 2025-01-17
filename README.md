# Kafka Plugin

## Как пользоваться:

1. **Создайте файл с расширением `.kafka` в своём проекте.**

2. **Подключение Kafka Producer**
    
   2.1. Вверху файла есть панель для подключения Kafka Producer, в поле `Bootstrap Servers:` необходимо ввести порты для подключения и нажать кнопку `Connect`.

   2.2. Пока происходит подключение, кнопка `Connect` блокируется. Если подключение успешно, то появляется уведомление _"Connected to Kafka successfully!"_. В противном случае даётся 30 секунд на подключение, и если подключение не удалось, то появляется диалоговое окно с ошибкой.

   2.3. Для закрытия Kafka Producer нажмите кнопку `Close Connection`.

![image](https://github.com/user-attachments/assets/cf9bd654-80d7-424c-a87f-7942147827aa)

3. **Подготовка сообщения для отправки**

   3.1. Напишите строку `###Send Message`, рядом с ней появится кнопка для того, чтобы спарсить подготовленное сообщение в диалоговое окно.

   3.2. Для сообщения можно подготовить ключ, заголовки, топик и содержимое сообщения в следующем формате:

    - **Key**: `Key:KeyValue` _(обязательно в одну строку)_

    - **Topic**: `Topic:TopicValue` _(обязательно в одну строку)_

    - **Message**:
      ```
      Message:
      MessageValue
      ```
      _(обязателен отступ строки после строки "Message")_
   
    - **Headers**:
      ```
      Headers:
      key1:value1
      key2:value2
      .etc
      ```
      _(каждое новое значение `key:value` пишется в новой строке и строго в одну строку)_

![image](https://github.com/user-attachments/assets/fd33ed6f-103b-442a-a339-5901db69eea9)

4. **Отправка сообщения**

   4.1. Для отправки сообщения необходимо нажать кнопку рядом с `###Send Message`, после чего откроется диалоговое окно, в котором будут введены данные для отправки сообщения.

   4.2. При необходимости можно изменить любые значения в диалоговом окне.

   4.3. Если оставить поле **Create Topic** пустым, то сообщение будет отправлено только в том случае, если топик уже существует. Если существование топика необязательно и вы готовы к тому, что топик может быть создан при отправке сообщения, нужно поставить галочку в поле **Create Topic**.

   4.4. Когда сообщение полностью готово к отправке, нажмите кнопку `Ok`.

   4.5. Если сообщение успешно отправлено, диалоговое окно закроется и появится уведомление об успешной отправке сообщения.

   4.6. Если сообщение не получилось отправить сразу, то блокируются кнопки `Ok` во всех диалоговых окнах, которые вы попытаетесь открыть, чтобы отправить сообщение (на отправку даётся 30 секунд).

![image](https://github.com/user-attachments/assets/69405312-1356-4320-a325-58bca2f1424b)

## P.S.

1. В каждом файле каждый блок информации для отправки сообщения необходимо отделять строкой `###Send Message` (в файле может быть любое количество таких блоков).

2. Если отправка сообщения или подключение Kafka Producer будет неудачным, то появится диалоговое окно с ошибкой и все кнопки разблокируются.

3. Закрытие Kafka Producer осуществляется вручную с помощью нажатия на кнопку `Close Connection`.

4. В самом верху диалогового окна указаны порты брокеров Kafka, в которые будет отправлено сообщение в случае успеха.

5. В одном проекте можно одновременно подключиться только к одному кластеру Kafka.
