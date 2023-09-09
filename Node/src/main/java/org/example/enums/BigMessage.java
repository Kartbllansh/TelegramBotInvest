package org.example.enums;

import com.vdurmont.emoji.EmojiParser;

public class BigMessage {
    public static final String CONSENT_MESSAGE = "Правила использования бота для обучения инвестированию \n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Цель бота: Данный бот предоставляет обучающий опыт в области инвестирования с использованием исторических данных. Все операции, проводимые в боте, не имеют реального финансового воздействия и не связаны с реальными рисками и доходами.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Риски и ответственность: Пожалуйста, поймите, что обучение с использованием этого бота не гарантирует будущего успеха на реальных финансовых рынках. Администрация бота не несет ответственности за любые возможные убытки или риски, возникшие из-за ваших решений, основанных на обучении с помощью этого бота.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Финансовая консультация: Бот не предоставляет финансовых советов и не заменяет профессионального финансового консультанта. Принимайте решения об инвестировании на реальных рынках только после консультации с квалифицированным финансовым специалистом.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Конфиденциальность данных: Ваши персональные данные и активности в боте будут храниться конфиденциально и не будут передаваться третьим сторонам без вашего согласия.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Информационная цель: Весь материал, предоставляемый в боте, включая тексты, графику и данные, предоставляется исключительно в информационных целях. Ни один элемент информации не должен рассматриваться как рекомендация к покупке или продаже ценных бумаг или других активов.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Авторские права: Все материалы, предоставляемые ботом, защищены авторскими правами. Запрещается воспроизводство, распространение или модификация любых материалов без письменного разрешения администрации бота.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Согласие: Используя этот бот, вы соглашаетесь с вышеуказанными правилами и условиями. Если вы не согласны с данными правилами, прекратите использование бота.";
    public static final String LEARNING_MESSAGE = "Ваш бот для инвестиций предоставляет следующие основные команды:\n" +
            "\n" +
            EmojiParser.parseToUnicode(":small_orange_diamond:")+"/buy - эта команда позволяет вам приобретать инвестиционные активы. Процесс покупки состоит из трех основных этапов: выбор актива, определение количества акций для покупки и подтверждение операции. Если вы затрудняетесь с выбором актива, встроенный поиск поможет вам найти ключевую информацию, такую как код или тикер акции. Впрочем, вы также можете воспользоваться внешними поисковиками для этой цели.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":small_orange_diamond:")+"/sell - эта команда аналогична команде /buy, но выполняет обратную функцию, позволяя вам продавать ваши инвестиционные активы.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":small_orange_diamond:")+"/show_bag - использование этой команды позволяет вам просматривать общий обзор вашего инвестиционного портфеля. \n \nПример такого отчета приведен ниже:\n" +
            "\n" +
            "Ваш инвестиционный портфель"+EmojiParser.parseToUnicode(":school_satchel:")+"\n" +
            "\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+ " Сбербанк-п (SBERP) - 3 акции\n" +
            "Доход"+EmojiParser.parseToUnicode(":money_with_wings:")+": 454.34 | 7.6%\n" +
            "Время покупок и продаж"+EmojiParser.parseToUnicode(":hourglass:")+":\n" +
            "Покупка 3 акций в 2023-09-01 13:36\n" +
            "\n" +
            "На кошельке: 204.25₽\n" +
            "Стоимость всех активов: 9784.75₽\n" +
            "Пополнения за все время: 10000.00₽\n" +
            "Прибыль за все время"+EmojiParser.parseToUnicode(":chart:")+": 534.07₽ | 6%\n" +
            "\n" +
            EmojiParser.parseToUnicode(":small_orange_diamond:")+"/wallet - эта команда предназначена для управления вашим кошельком и финансовыми операциями.\n" +
            "\n" +
            EmojiParser.parseToUnicode(":small_orange_diamond:")+"Остальные доступные команды можно найти в левом нижнем меню." +
            "\n P.S Сейчас, первым делом, советуем вам пополнить баланс командой /wallet";

    public static final String HELP_MESSAGE = "Список доступных команд"+EmojiParser.parseToUnicode(":page_facing_up:")+" \n \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /cancel - отмена выполнения текущей команды;\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /registration - регистрация пользователя;\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /wallet - получить информацию о вашем кошельке \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /buy - покупка ценной бумаги; \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /sell - продажа ценной бумаги; \n"+
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /show_bag - посмотреть портфель; \n"+
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /support - если возникли проблемы, пишите; \n"+
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /development - будущие разработки; \n";
    public static final String START_MESSAGE = "Добро пожаловать в мир виртуальной торговли нашего бота! \nЗдесь ты можешь развивать свои навыки инвестирования," +
            " не рискуя реальными средствами. " +
            "\n \n"+EmojiParser.parseToUnicode(":bank: ")+" Биржи доступные для тренировки: \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Московская биржа \n" +
             EmojiParser.parseToUnicode(":large_blue_diamond:")+" Остальные в разработке \n \n" +
            "Не забывай, что все операции проводятся с виртуальными средствами, так что ты можешь экспериментировать и учиться без риска потери реальных денег"+EmojiParser.parseToUnicode(":money_with_wings:")+"\n" +
            "\n" +
            EmojiParser.parseToUnicode(":exclamation:")+"На данный момент цена акций изменяется раз в месяц. Мы работает над тем, чтобы получать актуальную цену чаще."+EmojiParser.parseToUnicode(":exclamation:")+ "\n \n" +
            "Если у тебя возникнут вопросы или нужна помощь, не стесняйся обращаться к команде поддержки через команду /support. ";

    public static final String DEVELOPMENT_MESSAGE = " Команда в разработке :) \n \nЛадно, все, все, пошутили \nВ разработке: \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Добавление бирж NYSE и NASDAQ \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Обновление цены несколько раз в день \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Добавление соревновательной части боту \n" +
            "\nЕсли у вас есть идеи, пишите @Kartbllansh !Мы обязательно рассмотрим вашу идею!";
}
