package com.traviumx.ui.home;

import com.traviumx.Pool;
import com.traviumx.bot.Account;
import com.traviumx.bot.Village;
import com.traviumx.ui.addaccount.AddAccountController;
import com.traviumx.utils.Database;
import com.traviumx.utils.Parser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;

public class HomeController {
    @FXML
    private BorderPane _root;

    @FXML
    private ImageView _tribeImage;

    @FXML
    private Label _playerName;

    @FXML
    private ImageView _heroStatusImage;

    @FXML
    private Label _heroStatusText;

    @FXML
    private Tooltip _heroStatusTooltip;

    @FXML
    private ProgressBar _healthBar;

    @FXML
    private Tooltip _healthBarTooltip;

    @FXML
    private ProgressBar _xpBar;

    @FXML
    private Tooltip _xpBarTooltip;

    @FXML
    private Label _gameWorldText;

    @FXML
    private ImageView _heroImage;

    @FXML
    private ComboBox<Account> _accountList;

    @FXML
    private Button _addAccount;

    @FXML
    private ComboBox<Village> _villageList;

    @FXML
    private Label _warehouse;

    @FXML
    private ImageView _lumberBoost;

    @FXML
    private Label _lumber;

    @FXML
    private Tooltip _lumberTooltip;

    @FXML
    private ProgressBar _lumberBar;

    @FXML
    private ImageView _clayBoost;

    @FXML
    private Label _clay;

    @FXML
    private Tooltip _clayTooltip;

    @FXML
    private ProgressBar _clayBar;

    @FXML
    private ImageView _ironBoost;

    @FXML
    private Label _iron;

    @FXML
    private Tooltip _ironTooltip;

    @FXML
    private ProgressBar _ironBar;

    @FXML
    private Label _granary;

    @FXML
    private ImageView _cropBoost;

    @FXML
    private Label _crop;

    @FXML
    private Tooltip _cropTooltip;

    @FXML
    private ProgressBar _cropBar;

    @FXML
    private Label _freecrop;

    @FXML
    private Tooltip _freecropTooltip;


    @FXML
    protected void initialize() {
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
            if (_villageList.getSelectionModel().isEmpty()) {
                _villageList.getSelectionModel().selectFirst();
            }

            updateVillage();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void updateVillage() {
        Village v = _villageList.getSelectionModel().getSelectedItem();

        _warehouse.setText(Parser.ToDotty(v.warehouse));

        _lumber.setText(Parser.ToDotty(v.lumber));
        _lumberBar.setProgress(v.lumberFullness);
        _lumberTooltip.setText(v.lumberTooltip);
        if (v.lumberBoost) {
            _lumberBoost.setVisible(true);
        } else {
            _lumberBoost.setVisible(false);
        }

        _clay.setText(Parser.ToDotty(v.clay));
        _clayBar.setProgress(v.clayFullness);
        _clayTooltip.setText(v.clayTooltip);
        if (v.clayBoost) {
            _clayBoost.setVisible(true);
        } else {
            _clayBoost.setVisible(false);
        }

        _iron.setText(Parser.ToDotty(v.iron));
        _ironBar.setProgress(v.ironFullness);
        _ironTooltip.setText(v.ironTooltip);
        if (v.ironBoost) {
            _ironBoost.setVisible(true);
        } else {
            _ironBoost.setVisible(false);
        }

        _granary.setText(Parser.ToDotty(v.granary));

        _crop.setText(Parser.ToDotty(v.crop));
        _cropBar.setProgress(v.cropFullness);
        _cropTooltip.setText(v.cropTooltip);
        if (v.cropBoost) {
            _cropBoost.setVisible(true);
        } else {
            _cropBoost.setVisible(false);
        }

        _freecrop.setText(Parser.ToDotty(v.freecrop));
        _freecropTooltip.setText(v.freecropTooltip);



        //TODO: YAĞMA LİSTESİ İNİT.

        //TODO: yağma listesi için gold üyelik ve askeri üst olması gerek
    }
}
