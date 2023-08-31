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
    public static final String LEARNING_MESSAGE = "Тут будет красивая справка, как пользоваться ботом. Может даже видео";

    public static final String HELP_MESSAGE = "Список доступных команд"+EmojiParser.parseToUnicode(":page_facing_up:")+" \n \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /cancel - отмена выполнения текущей команды;\n " +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /registration - регистрация пользователя;\n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /wallet - получить информацию о вашем кошельке \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /buy - покупка ценной бумаги; \n" +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /sell - продажа ценной бумаги; \n"+
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /support - если возникли проблемы, пишите; \n"+
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" /development - будущие разработки; \n";
    public static final String START_MESSAGE = "Добро пожаловать в мир виртуальной торговли нашего бота! \n Здесь ты можешь развивать свои навыки инвестирования," +
            " не рискуя реальными средствами. " +
            "\n \n"+EmojiParser.parseToUnicode(":bank: ")+" Биржи доступные для тренировки: \n " +
            EmojiParser.parseToUnicode(":large_blue_diamond:")+" Московская биржа \n" +
             EmojiParser.parseToUnicode(":large_blue_diamond:")+" Остальные в разработке \n \n " +
            "Не забывай, что все операции проводятся с виртуальными средствами, так что ты можешь экспериментировать и учиться без риска потери реальных денег"+EmojiParser.parseToUnicode(":money_with_wings:")+"\n" +
            "\n" +
            "На данный момент цена акций изменяется раз в месяц. Мы работает там тем, чтобы получать актуальную цену чаще. \n \n" +
            "Если у тебя возникнут вопросы или нужна помощь, не стесняйся обращаться к команде поддержки через команду /support. ";

    public static final String DEVELOPMENT_MESSAGE = " Команда в разработке :)";
}
