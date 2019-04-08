package com.traviumx.bot;

import com.traviumx.Pool;
import com.traviumx.utils.Parser;
import javafx.application.Platform;
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
import java.util.ArrayList;
import java.util.List;

public class Raid {
    public static class TargetVillage {
        public SimpleStringProperty name;
        public String player;
        public int population;
        public int coordinateX;
        public int coordinateY;
        public String tribe;
        public String alliance;

        public double getDistance(Village v) {
            return Math.round(Math.sqrt(Math.pow((this.coordinateX - v.coordinateX), 2) +
                    Math.pow((this.coordinateY - v.coordinateY), 2)) * 10) / 10.0;
        }


    }


    public static Task GetTargetVillages(Account a, Village v, double minDistance, double maxDistance,
                                         double minPopulation, double maxPopulation, ProgressBar pb) {
        return new Task<ObservableList<TargetVillage>>() {
            @Override
            public ObservableList<TargetVillage> call() throws IOException {
                double progress = 0.0;

                ObservableList<TargetVillage> list = FXCollections.observableList(new ArrayList<>());
                if (Pool.targetVillages.isEmpty()) {
                    Platform.runLater(() -> pb.setProgress(-1));
                    double s1ty = 0.7;
                    HttpRequestBase get = new HttpGet(a.getGameWorld().getUrl() + "statistiken.php?id=2");
                    Document doc = Jsoup.parse(a.executeRequest(get));
                    int pageCount = Integer.valueOf(doc.select(".paginator .number").last().text());
                    for (int p = 430; p <= pageCount; p++) {
                        System.out.println("başladı " + p);
                        try {
                            HttpRequestBase eGet = new HttpGet(a.getGameWorld().getUrl() + "statistiken.php?id=2&page=" + p);
                            Document eDoc = Jsoup.parse(a.executeRequest(eGet));
                            for (Element e : eDoc.select("#villages tbody tr.hover")) {
                                TargetVillage tv = new TargetVillage();
                                tv.name = new SimpleStringProperty(e.select(".vil").text());
                                tv.player = e.select(".pla").text();
                                tv.population = Integer.valueOf(e.select(".hab").text());
                                tv.coordinateX = Parser.ParseInt(e.select(".coordinateX").text());
                                tv.coordinateY = Parser.ParseInt(e.select(".coordinateY").text());
                                Pool.targetVillages.add(tv);
                            }
                            progress += s1ty / pageCount;
                            double finalProgress = progress;
                            Platform.runLater(() -> pb.setProgress(finalProgress));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //detailed filter now
                for (TargetVillage tv : Pool.targetVillages) {
                    double distance = tv.getDistance(v);
                    if (tv.population <= maxPopulation && tv.population >= minPopulation &&
                            distance <= maxDistance && distance >= minDistance) {

                        list.add(tv);
                    }
                }

                return list;
            }
        };
    }
}

