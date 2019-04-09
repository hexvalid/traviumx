package com.traviumx.bot;

import com.traviumx.utils.Cache;
import com.traviumx.utils.Parser;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Raid {
    public static class TargetVillage {
        private SimpleStringProperty name;
        private SimpleStringProperty player;
        private SimpleIntegerProperty population;
        private SimpleIntegerProperty totalpopulation;
        private SimpleDoubleProperty distance;
        private SimpleStringProperty tribe;
        private SimpleStringProperty alliance;
        private int coordinateX;
        private int coordinateY;
        private boolean detailedLoaded;

        private void setDistance(Village v) {
            this.distance = new SimpleDoubleProperty(Math.round(Math.sqrt(Math.pow((this.coordinateX -
                    v.coordinateX), 2) + Math.pow((this.coordinateY - v.coordinateY), 2)) * 10) / 10.0);
        }

        public String getName() {
            return name.get();
        }

        public String getPlayer() {
            return player.get();
        }

        public Integer getPopulation() {
            return population.get();
        }


        public Integer getTotalpopulation() {
            return totalpopulation.get();
        }

        public Double getDistance() {
            return distance.get();
        }

        public String getCoordinate() {
            return "(" + this.coordinateX + " | " + this.coordinateY + ")";
        }


        public String getTribe() {
            return tribe.get();
        }

        public String getAlliance() {
            return alliance.get();
        }
    }


    public static Task GetTargetVillages(Account a, Village v, double minDistance, double maxDistance,
                                         int minTotalPopulation, int maxTotalPopulation, boolean onlyNotHaveAlliance,
                                         boolean roman, boolean teuton, boolean gaul, boolean egyptian, boolean hun,
                                         ProgressBar pb) {
        return new Task<ObservableList<TargetVillage>>() {
            @Override
            public ObservableList<TargetVillage> call() throws IOException, SQLException {
                double progress = 0.0;

                ObservableList<TargetVillage> list = FXCollections.observableList(new ArrayList<>());
                ObservableList<TargetVillage> finalList = FXCollections.observableList(new ArrayList<>());

                //todo: cache denemesi örneği:
                boolean fromCache;
                List<TargetVillage> cachedList;
                cachedList = (List<TargetVillage>) Cache.GetFromCache("villages@" + a.getGameWorld().getUuid(), 48);
                if (cachedList == null) {
                    cachedList = new ArrayList<>();
                    fromCache = false;
                    Platform.runLater(() -> pb.setProgress(-1));
                    HttpRequestBase get = new HttpGet(a.getGameWorld().getUrl() + "statistiken.php?id=2");
                    Document doc = Jsoup.parse(a.executeRequest(get));
                    int pageCount = Integer.valueOf(doc.select(".paginator .number").last().text());
                    for (int p = 1; p <= pageCount; p++) {
                        System.out.println("başladı " + p);
                        try {
                            get = new HttpGet(a.getGameWorld().getUrl() + "statistiken.php?id=2&page=" + p);
                            doc = Jsoup.parse(a.executeRequest(get));
                            for (Element e : doc.select("#villages tbody tr.hover")) {
                                TargetVillage tv = new TargetVillage();
                                tv.name = new SimpleStringProperty(e.select(".vil").text());
                                tv.player = new SimpleStringProperty(e.select(".pla").text());
                                tv.population = new SimpleIntegerProperty(Integer.valueOf(e.select(".hab").text())); //todo contantine
                                tv.coordinateX = Parser.ParseInt(e.select(".coordinateX").text());
                                tv.coordinateY = Parser.ParseInt(e.select(".coordinateY").text());
                                cachedList.add(tv);
                            }
                            System.out.println("hmm ?");
                            progress += 0.4 / pageCount;
                            double finalProgress = progress;
                            Platform.runLater(() -> pb.setProgress(finalProgress));
                        } catch (IOException e) {
                        }
                    }
                } else {
                    fromCache = true;
                }

                //update distance
                for (TargetVillage tv : cachedList) {
                    tv.setDistance(v);
                }

                //fast filter now
                for (TargetVillage tv : cachedList) {
                    if (tv.getDistance() <= maxDistance && tv.getDistance() >= minDistance) {
                        int totalpopulation = 0;
                        for (TargetVillage tvx : cachedList) {
                            if (tv.getPlayer().equals(tvx.getPlayer())) {
                                totalpopulation += tvx.getPopulation();
                            }
                        }
                        tv.totalpopulation = new SimpleIntegerProperty(totalpopulation);
                        if (totalpopulation <= maxTotalPopulation && totalpopulation >= minTotalPopulation) {
                            list.add(tv);
                        }
                    }
                }


                //detailed filter now
                for (TargetVillage tv : list) {
                    if (fromCache) {
                        progress += 0.6 / list.size();
                    } else {
                        progress += 1.0 / list.size();
                    }
                    double finalProgress = progress;
                    Platform.runLater(() -> pb.setProgress(finalProgress));
                    if (!tv.detailedLoaded) {
                        HttpRequestBase get = new HttpGet(a.getGameWorld().getUrl() +
                                "position_details.php?x=" + tv.coordinateX + "&y=" + tv.coordinateY);
                        Document doc = Jsoup.parse(a.executeRequest(get));
                        tv.tribe = new SimpleStringProperty(doc.select("#village_info .first td").first().text());

                        tv.alliance = new SimpleStringProperty(doc.select("#village_info tbody td.alliance").first().text());
                        //todo: birliksiz: System.out.println("(" + tv.alliance + ") boş: " + tv.alliance.get().equals(""));
                        tv.detailedLoaded = true;

                    }

                    //seperation.
                    if (!(onlyNotHaveAlliance && !tv.alliance.get().equals("")) &&
                            !(!roman && tv.getTribe().equals("Romalılar")) &&
                            !(!teuton && tv.getTribe().equals("Cermenler")) &&
                            !(!gaul && tv.getTribe().equals("Galyalılar")) &&
                            !(!egyptian && tv.getTribe().equals("Mısırlılar")) &&
                            !(!hun && tv.getTribe().equals("Hunlar"))) {
                        //todo: AYRI BİR FONKSİYONDA OLACAK BU. HER 2 TARAFA DÖNÜŞTÜRMELİ HEMDE.
                        finalList.add(tv);
                    }


                }


                if (fromCache) {
                    Cache.UpdateFromCache("villages@" + a.getGameWorld().getUuid(), cachedList);
                } else {
                    Cache.AddToCache("villages@" + a.getGameWorld().getUuid(), cachedList);
                }
                return finalList;
            }
        };
    }
}

