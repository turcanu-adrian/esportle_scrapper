
import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;


//team, players country, players continent, age, earnings, hltv rating, full name, name
class CSPlayer {
    public String fullname, name, currentTeam, country, continent;
    public int age, earnings;
    public float rating;
    public ArrayList<String> pastTeams;
    public CSPlayer() {
        this.fullname=null;
        this.name=null;
        this.currentTeam=null;
        this.country=null;
        this.continent=null;
        this.age=0;
        this.earnings=0;
        this.rating=0;
        this.pastTeams= new ArrayList<>();
    }
}


public class main {

    static ArrayList<String> CIS = new ArrayList<>(Arrays.asList("Russia", "Belarus", "Ukraine", "Azerbaijan", "Kazakhstan", "Uzbekistan", "Kyrgyzstan"));
    static ArrayList<String> Americas = new ArrayList<>(Arrays.asList("Canada", "United States", "Mexico", "Guatemala", "Colombia", "Venezuela", "Ecuador", "Peru", "Brazil", "Uruguay", "Argentina", "Chile"));
    static ArrayList<String> Oceania = new ArrayList<>(Arrays.asList("Australia", "New Zealand"));
    static ArrayList<String> SEA = new ArrayList<>(Arrays.asList("China", "South Korea", "Mongolia", "Vietnam", "India", "Pakistan", "Thailand", "Singapore", "Indonesia", "Malaysia", "Taiwan", "Hong Kong"));
    static ArrayList<String> AME = new ArrayList<>(Arrays.asList("Iraq", "Iran", "Israel", "Jordan", "Saudi Arabia", "Syria", "United Arab Emirates", "Lebanon", "South Africa", "Tunisia"));

     static void generateValorantNames(String region) throws IOException {
        //CREATING FILE
        File myObj = new File("valplayers.js");
        if (myObj.createNewFile())
            System.out.println("File created: " + myObj.getName());
        else
            System.out.println("File already exists.");

        FileWriter myWriter = new FileWriter("valplayers.js", false);

        ArrayList<String> playernames = new ArrayList<>();
        Document playerspage = Jsoup.connect("https://liquipedia.net/valorant/Portal:Players/" +region).get();

        Elements tables = playerspage.getElementsByClass("wikitable collapsible smwtable");


        for (Element table : tables)
            if (!table.getElementsByTag("td").get(2).text().equals(""))
                playernames.add(table.getElementsByTag("td").get(0).text());

        String gson = new Gson().toJson(playernames);
        myWriter.write(gson);
        myWriter.close();
    }

    static void generateCSGONames() throws IOException {
        //CREATING FILE
        File myObj = new File("csplayersnames.js");
        if (myObj.createNewFile())
            System.out.println("File created: " + myObj.getName());
        else
            System.out.println("File already exists.");

        FileWriter myWriter = new FileWriter("csplayersnames.js", false);

        ArrayList<String> playernames = new ArrayList<>();
        Document playerspage = Jsoup.connect("https://www.hltv.org/ranking/teams").get();
        Elements lineups = playerspage.getElementsByClass("nick");

        for (Element lineup : lineups)
            playernames.add(lineup.text());

        String gson = new Gson().toJson(playernames);
        myWriter.write(gson);
        myWriter.close();

    }

    static void generateTeamLogos() throws IOException {
        Document playerspage = Jsoup.connect("https://www.hltv.org/ranking/teams").get();
        Elements teamLogos = playerspage.getElementsByClass("team-logo");

        for (Element teamLogo : teamLogos) {
            String link = teamLogo.getElementsByTag("img").first().attr("src");
            if (link.contains(".svg")) {
                File teamlogo = new File("team-logos/" + teamLogo.getElementsByTag("img").first().attr("title") + ".svg");
                FileUtils.copyURLToFile(new URL(link), teamlogo);
            } else {
                File teamlogo = new File("team-logos/" + teamLogo.getElementsByTag("img").first().attr("title") + ".png");
                FileUtils.copyURLToFile(new URL(link), teamlogo);
            }
        }
     }

    static ArrayList<String> generateCSGOlinks() throws IOException {
        ArrayList<String> playerlinks = new ArrayList<>();
        Document playerspage = Jsoup.connect("https://www.hltv.org/ranking/teams").get();
        Elements lineups = playerspage.getElementsByClass("pointer");

        for (Element lineup : lineups)
            playerlinks.add(lineup.attr("href"));

        return playerlinks;
    }

    static void generateCSGOplayer (String playerlink, ArrayList<CSPlayer> csPlayers) throws Exception{
        //, players continent, , earnings
        Document playerspage = Jsoup.connect("https://www.hltv.org/"+ playerlink).get();
        Element playerProfile = playerspage.getElementsByClass("playerProfile").first();
        CSPlayer player = new CSPlayer();
        player.name = playerProfile.getElementsByClass("playerNickname").first().text();
        player.fullname=playerProfile.getElementsByClass("playerRealname").first().text().split(" ", 2)[0] + " \""+player.name + "\" " + playerProfile.getElementsByClass("playerRealname").first().text().split(" ", 2)[1];
        player.age=Integer.parseInt(playerProfile.getElementsByClass("playerAge").first().getElementsByClass("listRight").first().text().substring(0,2));
        player.currentTeam=playerProfile.getElementsByClass("playerTeam").first().getElementsByClass("listRight").first().text();
        player.country = playerProfile.getElementsByClass("playerRealname").first().getElementsByClass("flag").first().attr("title");

        if (playerProfile.getElementsByClass("player-stat").first() != null)
            player.rating = Float.parseFloat(playerProfile.getElementsByClass("player-stat").first().getElementsByClass("statsVal").first().text());
        else
            player.rating = 0;

        for (Element team : playerProfile.getElementsByClass("team-name gtSmartphone-only"))
            player.pastTeams.add(team.text());
        player.pastTeams.remove(0);

        if (CIS.contains(player.country))
            player.continent="CIS";
        else if (Americas.contains(player.country))
            player.continent="Americas";
        else if (Oceania.contains(player.country))
            player.continent="Oceania";
        else if (SEA.contains(player.country))
            player.continent = "Eastern & Southern Asia";
        else if (AME.contains(player.country))
            player.continent = "Africa & Middle East";
        else
            player.continent = "Europe";


        Document googlesearch = Jsoup.connect("https://www.google.com/search?q=esportsearnings+"+ player.name+"+"+player.country +"+CSGO").get();
        String googleresult = googlesearch.getElementsByClass("yuRUbf").first().getElementsByTag("a").first().attr("href");
        Document liquipediapage = Jsoup.connect(googleresult).get();

        for (Element cell : liquipediapage.getElementsByClass("info_prize_highlight"))
            if (cell.text().charAt(0) == '$')
            {
                NumberFormat nF  = NumberFormat.getNumberInstance();
                player.earnings = nF.parse(cell.text().substring(1)).intValue();
                break;
            }
        csPlayers.add(player);

    }

    public static void main(String[] args) throws Exception {
        try {
            File myObj = new File("csplayers.js");
            if (myObj.createNewFile())
                System.out.println("File created: " + myObj.getName());
            else
                System.out.println("File already exists.");

            FileWriter myWriter = new FileWriter("csplayers.js", false);

            generateCSGONames();
            ArrayList<String> playerlist;
            playerlist = generateCSGOlinks();
            ArrayList<CSPlayer> CSPlayerList = new ArrayList<>();
            int index = 0;
            for (String playerlink : playerlist) {
                generateCSGOplayer(playerlink, CSPlayerList);
                System.out.println("generated player " + playerlink);
                index++;
                System.out.println("players left: " + (playerlist.size()+1-index));
                Thread.sleep(30000);
            }
            String gson = new Gson().toJson(CSPlayerList);
            myWriter.write(gson);
            myWriter.close();
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        generateTeamLogos();
}}
