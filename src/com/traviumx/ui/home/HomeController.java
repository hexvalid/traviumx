package com.traviumx.ui.home;

import com.traviumx.Pool;
import com.traviumx.bot.Account;
import com.traviumx.bot.Village;
import com.traviumx.ui.addaccount.AddAccountController;
import com.traviumx.utils.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;

public class HomeController {

    @FXML
    private BorderPane _root;

    @FXML
    private Button _addAccount;

    @FXML
    private ComboBox<Account> _accountList;

    @FXML
    private ImageView _heroImage;

    @FXML
    private ImageView _tribeImage;

    @FXML
    private Label _playerName;

    @FXML
    private ProgressBar _healthBar;

    @FXML
    private Tooltip _healthBarTooltip;

    @FXML
    private ProgressBar _xpBar;

    @FXML
    private Tooltip _xpBarTooltip;

    @FXML
    private ImageView _heroStatusImage;

    @FXML
    private Label _heroStatusText;

    @FXML
    private Tooltip _heroStatusTooltip;

    @FXML
    private VBox _heroBox;

    @FXML
    private Label _gameWorldText;

    @FXML
    private ComboBox<Village> _villageList;

    @FXML
    protected void initialize() {
        _heroBox.setVisible(false);
        _accountList.setCellFactory(accountCallBack);
        _accountList.setButtonCell(accountCallBack.call(null));
        _villageList.setCellFactory(villageCallBack1);
        _villageList.setButtonCell(villageCallBack2.call(null));
        updateAccountList();

    }

    @FXML
    public void addAccount() {
        try {
            _addAccount.setDisable(true);
            Parent root = FXMLLoader.load(AddAccountController.class.getResource("addaccount.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(_root.getScene().getWindow());
            stage.setResizable(false);
            stage.setAlwaysOnTop(true);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            _addAccount.setDisable(false);
            updateAccountList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Callback<ListView<Account>, ListCell<Account>> accountCallBack = new Callback<ListView<Account>, ListCell<Account>>() {
        @Override
        public ListCell<Account> call(ListView<Account> p) {
            return new ListCell<Account>() {
                @Override
                protected void updateItem(Account a, boolean bln) {
                    super.updateItem(a, bln);
                    if (a != null) {
                        setText(a.getUsername());
                    } else {
                        setText(null);
                    }
                }
            };
        }
    };


    private static Callback<ListView<Village>, ListCell<Village>> villageCallBack1 = new Callback<ListView<Village>, ListCell<Village>>() {
        @Override
        public ListCell<Village> call(ListView<Village> p) {
            return new ListCell<Village>() {
                @Override
                protected void updateItem(Village v, boolean bln) {
                    super.updateItem(v, bln);
                    if (v != null) {
                        setText(v.name + "\t(" + v.coordinateX + " | " + v.coordinateY + ")");
                    } else {
                        setText(null);
                    }
                }
            };
        }
    };

    private static Callback<ListView<Village>, ListCell<Village>> villageCallBack2 = new Callback<ListView<Village>, ListCell<Village>>() {
        @Override
        public ListCell<Village> call(ListView<Village> p) {
            return new ListCell<Village>() {
                @Override
                protected void updateItem(Village v, boolean bln) {
                    super.updateItem(v, bln);
                    if (v != null) {
                        setText(v.name);
                    } else {
                        setText(null);
                    }
                }
            };
        }
    };

    @FXML
    public void updateAccountList() {
        try {
            Database.getAccountsToPool();
            _accountList.getItems().setAll(Pool.accountList);
            if (_accountList.getSelectionModel().getSelectedItem() == null) {
                _accountList.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadAccount() {
        Account a = _accountList.getSelectionModel().getSelectedItem();
        try {
            a.Load();
            _playerName.setText(a.PlayerName);
            _tribeImage.setImage(new Image(getClass().getResourceAsStream("/com/traviumx/ui/img/" + a.Tribe + ".png")));
            _heroStatusImage.setImage(new Image(getClass().getResourceAsStream("/com/traviumx/ui/img/" + a.HeroStatus + ".png")));
            _heroStatusText.setText(a.HeroStatusText);
            _heroStatusTooltip.setText(a.HeroStatusTooltip);
            _heroImage.setImage(a.HeroImage);
            _healthBar.setProgress(a.Health);
            _healthBarTooltip.setText(a.HealthTooltip);
            _xpBar.setProgress(a.Experience);
            _xpBarTooltip.setText(a.ExperienceTooltip);
            _gameWorldText.setText(a.getGameWorld().getName());
            _villageList.getItems().setAll(a.Villages);
            if (_villageList.getSelectionModel().isEmpty()){
                _villageList.getSelectionModel().selectFirst();
            }
            _heroBox.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
