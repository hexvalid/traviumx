package com.traviumx.ui.addaccount;

import com.traviumx.bot.Account;
import com.traviumx.bot.GameWorld;
import com.traviumx.bot.Vars;
import com.traviumx.ui.Helper;
import com.traviumx.utils.Database;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class AddAccountController {

    @FXML
    private RowConstraints _userAgentRow;

    @FXML
    private RowConstraints _pinRow;

    @FXML
    private ComboBox<GameWorld> _gameWorlds;

    @FXML
    private TextField _username;

    @FXML
    private PasswordField _password;

    @FXML
    private CheckBox _usePin;

    @FXML
    private Label _userAgentLabel;

    @FXML
    private TextArea _pin;

    @FXML
    private Label _pinLabel;

    @FXML
    private TextField _userAgent;

    @FXML
    private Hyperlink _whatsMyUserAgentLink;

    @FXML
    private Label _status;

    @FXML
    private Button _cancel;

    @FXML
    private Button _addAccount;


    @FXML
    protected void initialize() {
        //todo: dont throw exception!
        _userAgent.setPromptText(Vars.DefaultUserAgent);
        _pinRow.setPrefHeight(0);
        _pin.setVisible(false);
        _pinLabel.setVisible(false);
        _gameWorlds.setDisable(true);
        _gameWorlds.setPromptText("Yükleniyor...");

        new Thread(() -> {
            try {
                List<GameWorld> l = GameWorld.GetGameWorlds("tr", true);
                Platform.runLater(() -> {
                    _gameWorlds.setCellFactory(gameWorldCallBack);
                    _gameWorlds.setButtonCell(gameWorldCallBack.call(null));
                    _gameWorlds.getItems().addAll(FXCollections.observableArrayList(l));
                    _gameWorlds.setPromptText("");
                    _gameWorlds.getSelectionModel().selectFirst();
                    _gameWorlds.setDisable(false);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    @FXML
    void usePinAction() {
        if (_usePin.isSelected()) {
            _userAgentRow.setPrefHeight(0);
            _userAgentLabel.setPrefHeight(0);
            _userAgentLabel.setPrefWidth(0);
            _userAgent.setPrefHeight(0);
            _userAgent.setPrefWidth(0);
            _userAgentLabel.setPrefHeight(0);
            _userAgentLabel.setPrefWidth(0);
            _userAgentLabel.setVisible(false);
            _userAgent.setVisible(false);
            _userAgentLabel.setVisible(false);
            _pin.setVisible(true);
            _pinLabel.setVisible(true);
            _pinRow.setPrefHeight(Control.USE_COMPUTED_SIZE);
        } else {
            _pinRow.setPrefHeight(0);
            _userAgentLabel.setPrefHeight(Control.USE_COMPUTED_SIZE);
            _userAgentLabel.setPrefWidth(Control.USE_COMPUTED_SIZE);
            _userAgent.setPrefHeight(Control.USE_COMPUTED_SIZE);
            _userAgent.setPrefWidth(Control.USE_COMPUTED_SIZE);
            _whatsMyUserAgentLink.setPrefHeight(Control.USE_COMPUTED_SIZE);
            _whatsMyUserAgentLink.setPrefWidth(Control.USE_COMPUTED_SIZE);
            _userAgentLabel.setVisible(true);
            _userAgent.setVisible(true);
            _whatsMyUserAgentLink.setVisible(true);
            _pin.setVisible(false);
            _pinLabel.setVisible(false);
            _userAgentRow.setPrefHeight(Control.USE_COMPUTED_SIZE);
        }
    }

    @FXML
    void whatsMyUserAgent() {
        //todo: bu link prefsde olmalı
        Helper.OpenURL("http://whatsmyuseragent.org/");
    }

    @FXML
    void buyPin() {
        Helper.OpenURL("https://t.me/hexvalid");
    }

    private static Callback<ListView<GameWorld>,
            ListCell<GameWorld>> gameWorldCallBack = new Callback<ListView<GameWorld>, ListCell<GameWorld>>() {
        @Override
        public ListCell<GameWorld> call(ListView<GameWorld> p) {
            return new ListCell<GameWorld>() {
                @Override
                protected void updateItem(GameWorld gw, boolean bln) {
                    super.updateItem(gw, bln);
                    if (gw != null) {
                        setText(gw.getName() + " (" + gw.getPrettyUrl() + ")");
                    } else {
                        setText(null);
                    }
                }
            };
        }
    };

    @FXML
    void quit() {
        Stage stage = (Stage) _cancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void addAccount() {
        new Thread(() -> {
            Account a;
            try {
                Platform.runLater(() -> _status.setText("Giriş yapılıyor..."));
                String useragent;
                if (_userAgent.getText().length() > 8) {
                    useragent = _userAgent.getText();
                } else {
                    useragent = Vars.DefaultUserAgent;
                }
                a = new Account(_username.getText(), _password.getText(), _gameWorlds.getValue(), useragent, _pin.getText());
                try {
                    a.Login();
                    Database.AddAccountToDB(a);
                    Platform.runLater(this::quit);
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> _status.setText(e.getMessage()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }


}
