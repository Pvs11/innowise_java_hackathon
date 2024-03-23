package cryptocurrencyBotApplication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cryptocurrencyBotApplication.config.BotConfig;
import cryptocurrencyBotApplication.exceptions.UrlNotFoundException;
import cryptocurrencyBotApplication.model.Cryptocurrency;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Component
public class CryptocurrencyBot extends TelegramLongPollingBot {
    private double userChoice;
    private List<Cryptocurrency> previousCurrencies = getCryptoList();
    private static final Map<String, String> percentageButtons = Map.of("3%", "THREE", "5%", "FIVE", "10%", "TEN", "15%", "FIFTEEN");

    private static final String RESOURCE_URL = "https://api.mexc.com/api/v3/ticker/price";
    private final BotConfig config;
    @Autowired
    public CryptocurrencyBot(BotConfig config) throws IOException {
        this.config = config;
    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
    public void sendNotificationConstantly() throws SchedulerException {
        // should be invoked method checkChangesAndReturn(userChoice, previousCurrencies) every 20 sec
        // and if it is not empty send message to user with all changes.
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!Objects.equals(update.getMessage(), null) && !Objects.equals(update.getMessage().getText(), null)) {
            String message = update.getMessage().getText();
            Chat chat = update.getMessage().getChat();
            long chatId = chat.getId();
            switch (message) {
                case "/start" -> greetUser(update);
                case "/help" -> sendHelpInfo(chatId);
                case "/currentRate" -> sendCryptocurrencyList(chatId);
                case "/choose" -> sendNotificationOptions(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            if (query.equals("THREE")) {
                userChoice = 3;
                SendMessage sm = SendMessage.builder().chatId(query.getId()).text("Great").build();
                executeSendMessage(sm);
            }
            if (query.equals("FIVE")) {
                userChoice = 5;
            }
            if (query.equals("TEN")) {
                userChoice = 10;
            }
            if (query.equals("FIFTEEN")) {
                userChoice = 15;
            }
        }
    }
    private List<Cryptocurrency> checkChangesAndReturn(double percent, List<Cryptocurrency> previous) {
        List<Cryptocurrency> newCurrencies = null;
        try {
            newCurrencies = getCryptoList();
        } catch (IOException e) {
            throw new UrlNotFoundException();
        }
        List<Cryptocurrency> cryptocurrenciesWithChanges = new ArrayList<>();
        for (int i = 0; i < previous.size(); i++) {
            Cryptocurrency previousCurrency = previous.get(i);
            String symbol = previousCurrency.getSymbol();
            double newPrice = newCurrencies.stream().filter(c->c.getSymbol().equals(symbol)).findFirst().get().getPrice();
            if (Math.abs ((previousCurrency.getPrice() / newPrice) * 100 - 100) >= percent) {
                cryptocurrenciesWithChanges.add(newCurrencies.get(i));
            }
            previousCurrencies = newCurrencies;
        }
        return cryptocurrenciesWithChanges;

    }
    private void sendNotificationOptions(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = createInlineKeyboard(percentageButtons);
        SendMessage sm = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Please, choose percent when you want to see update in cryptocurrency")
                .replyMarkup(keyboardMarkup)
                .build();
        executeSendMessage(sm);
    }

    private void greetUser(Update update) {
        Chat chat = update.getMessage().getChat();
        long chatId = chat.getId();

        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "Starts this bot with basic info"));
        botCommands.add(new BotCommand("/help", "help yourself"));
        botCommands.add(new BotCommand("/choose", "Choose required notification frequency"));
        try {
            execute(new SetMyCommands(botCommands, new BotCommandScopeChat(String.valueOf(chatId)), "en"));
        } catch (TelegramApiException e) {
            e.fillInStackTrace();
        }
        String userName = chat.getFirstName();
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton currentcryptoButton = new KeyboardButton("/currentRate");
        row.add(currentcryptoButton);
        keyboardMarkup.setKeyboard(List.of(row));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        String greetings = String.format("Hi, %s I am a cryptocurrencyBot, I provide information about actual crypocurrency rates. Please, use buttons below to see current values.", userName);
        SendMessage sendMessage = SendMessage.builder().chatId(String.valueOf(chatId)).text(greetings).replyMarkup(keyboardMarkup).build();
        executeSendMessage(sendMessage);
    }

    private InlineKeyboardMarkup createInlineKeyboard(Map<String, String> buttonNameAndCallbackName) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rows = new ArrayList<>();
        Set<Map.Entry<String, String>> set = buttonNameAndCallbackName.entrySet();
        Iterator<Map.Entry<String, String>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(entry.getKey());
            button.setCallbackData(entry.getValue());
            rows.add(button);
        }
        List<List<InlineKeyboardButton>> severalRows = List.of(rows);
        keyboard.setKeyboard(severalRows);
        return keyboard;
    }

    private void sendCryptocurrencyList(long chatId) {
        List<Cryptocurrency> cryptocurrencyList = null;
        try {
            cryptocurrencyList = getCryptoList();
        } catch (IOException e) {
            throw new UrlNotFoundException();
        }
        executeSendMessage(SendMessage.builder().chatId(String.valueOf(chatId)).text("Here the list of all cryptos and prices").build());
        for (int i = 0; i < cryptocurrencyList.size(); i++) {
            StringBuilder sb = new StringBuilder();
            Cryptocurrency cryptocurrency = cryptocurrencyList.get(i);
            sb.append(cryptocurrency.getSymbol()).append(" - ").append(cryptocurrency.getPrice()).append("\n");
        SendMessage sm = SendMessage.builder().chatId(String.valueOf(chatId)).text(sb.toString()).build();
        executeSendMessage(sm);
        }
    }

    private List<Cryptocurrency> getCryptoList() throws IOException{
        URL url = null;
        try {
            url = new URL(RESOURCE_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        List<Cryptocurrency> cryptocurrencyList = null;
        try {
            cryptocurrencyList = objectMapper.readValue(url, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cryptocurrencyList;
    }

    private void executeSendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendHelpInfo(long chatId) {
        SendMessage sm = SendMessage.builder().chatId(String.valueOf(chatId)).text("help yourself!)").build();
        executeSendMessage(sm);
    }

}
