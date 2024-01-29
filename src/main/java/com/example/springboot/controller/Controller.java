package com.example.springboot.controller;

import com.example.springboot.models.*;
import com.example.springboot.services.LobbyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Import the GameLobby class

import static com.example.springboot.services.LobbyService.generateGameId;

@RestController
@RequestMapping(path="/api/game")
public class Controller {
    @Autowired
    private LobbyService lobbyService;

    private final RestTemplate restTemplate;

    @Autowired
    public Controller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private final List<String> hiQAreas = Arrays.asList(
            "Repan", "X", "Pingisbordet", "Speedys korg", "Labbet",
            "War Room", "FIFA-soffan", "Scania-rummet"
    );

    private final List<String> sports = Arrays.asList(
            "Soccer", "Basketball", "Cricket", "Baseball", "Tennis",
            "Golf", "Swimming", "Athletics", "Gymnastics", "Boxing",
            "Cycling", "Volleyball", "Table Tennis", "Badminton", "Rugby",
            "Hockey", "Skiing", "Snowboarding", "Skateboarding", "Surfing",
            "Horse Racing", "Fencing", "Archery", "Bowling", "Darts",
            "Bouldering", "Karate", "Judo", "Taekwondo", "Wrestling",
            "Ice Hockey", "Figure Skating", "Bobsleigh", "Curling", "Luge",
            "Biathlon", "Triathlon", "Pentathlon", "Rowing", "Sailing",
            "Canoeing", "Kayaking", "Water Polo", "Diving", "Synchronized Swimming",
            "Polo", "Lacrosse", "Handball", "Squash", "Rock Climbing"
    );

    private final List<String> programmingLanguages = Arrays.asList(
            "Java", "Python", "C++", "C#", "JavaScript",
            "Ruby", "Swift", "Kotlin", "Go", "R",
            "PHP", "TypeScript", "Scala", "Perl", "Lua",
            "Haskell", "Elixir", "Rust", "Dart", "Objective-C"
    );

    private final List<String> itTitles = Arrays.asList(
            "Software Developer", "Systems Analyst", "Network Administrator", "Database Administrator",
            "IT Manager", "Security Analyst", "Cloud Architect", "Data Scientist",
            "Front-end Developer", "Back-end Developer", "Full-stack Developer", "DevOps Engineer",
            "Quality Assurance Tester", "User Experience Designer", "Technical Support Specialist",
            "IT Project Manager", "Machine Learning Engineer", "Cybersecurity Specialist",
            "Business Intelligence Analyst", "Web Developer", "Recruiter", "Salesperson", "CEO"
    );


    public static final List<String> promptsDefault = Arrays.asList(
            "Det bästa sättet att överleva en zombieapokalyps är att...",
            "En avvisad smak för glass: ____-smaksatt.",
            "Ett märkligt namn för en rymdraket.",
            "Om utomjordingar besökte jorden, det första de skulle säga är...",
            "Det sämsta jobbet i ett sagorike.",
            "Den verkliga anledningen till att dinosaurierna är utdöda.",
            "Uppfinn en ny högtid som ingen skulle vilja fira.",
            "En bisarr ingrediens att lägga till i en smoothie.",
            "Den hemliga talangen din brödrost har när du inte tittar.",
            "En ny lag som alla måste följa: Inget mer ____ på tisdagar.",
            "Ett hemskt tema för en studentbal.",
            "En rubrik från år 3000.",
            "Det sämsta att säga under en anställningsintervju.",
            "Det sämsta raggningsrepliken på en begravning.",
            "Den minst imponerande superkraften.",
            "Om du kunde byta liv med en historisk person, vem skulle det vara och varför?",
            "Ett tvivelaktigt alternativt användningsområde för silvertejp.",
            "Titeln på en kontroversiell realityshow om ditt liv.",
            "En överraskande plats för en romantisk weekend.",
            "Om ditt liv var en cocktail, vad skulle huvudingrediensen vara?",
            "Den mest olämpliga tiden att börja dansa.",
            "Ett kontroversiellt användningsområde för en tidsmaskin.",
            "Det sista du vill höra från din personliga tränare.",
            "Ett bisarrt tillskott till en romantisk spellista.",
            "Om ditt kylskåp kunde skvallra, vad skulle det säga om dina midnattssnacks?",
            "En konstig sak att ta med till en nudiststrand.",
            "Det sämsta möjliga ämnet för en ståupp-komedi rutin.",
            "Ett kontroversiellt namn för en dejtingapp.",
            "En konstig superkraft att ha i vardagen.",
            "Nyckeln till fred på jorden.",
            "Tre ord för att beskriva <--->.",
            "<--->'s största svaghet.",
            "<--->'s hemliga internet-nickname",
            "<--->'s mörkaste hemlighet",
            "Om <---> startade ett politiskt parti, vad skulle det heta?",
            "Om <---> var en superhjälte skulle hens superkraft vara ____",
            "Om <---> var en stand-up komiker, vilket kontroversiellt ämne skulle hen skämta om?"
    );

    public static final List<String> promptsHiq = Arrays.asList(
            "Den sämsta ursäkten för att ringa in sjuk till jobbet.",
            "Om HiQ hade en maskot, vad skulle det vara och varför?",
            "En ny HiQ-företagspolicy: ____ på fredagar.",
            "Den oskrivna regeln i HiQ:s anställningshandbok.",
            "En nekad HiQ-slogan för en motivationsaffisch.",
            "Det mest oväntade objektet i HiQ:s kylskåp.",
            "En konstig talang din chef har som ingen pratar om.",
            "Om kontorsmaterial hade personligheter, vilket skulle du vara vän med och varför?",
            "En konstig sak att säga under standup på Magic Monday.",
            "En olämplig aktivitet under ett KM.",
            "HiQ Town Hall annonserar nästa konsultgrupp!: ____",
            "Den största anledningen till HiQ's tillväxt",
            "Den nya satsningen för marknadsföring av HiQ innefattar ____",
            "Temat för nästa julfest",
            "Det bästa sättet att få in nya kunder.",
            "Repan har köpt in ____, produktiviteten stiger!",
            "Vad <---> gör i labbet.",
            "LEAP-programmet stängs ned, följden blir"
    );

    //@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/host")
    public ResponseEntity<Map<String, Object>> hostGame(@RequestBody Map<String, String> request) {
        String username     = request.get("username");
        String gameChoice   = request.get("gameChoice");
        String imageUrl     = request.get("imageUrl");

        String gameId = generateGameId();

        // 30 min timeout for lobby
        Date currentTime = new Date();
        Date timeout = new Date(currentTime.getTime() + 60000 * 30);
        int timeLeftInSeconds = Math.toIntExact((timeout.getTime() - currentTime.getTime()) / 1000);

        // Create new GameLobby object
        GameLobby newLobby = new GameLobby(username, timeLeftInSeconds, gameChoice);
        newLobby.addPlayer(username, imageUrl);

        // Add GameLobby to list of game lobbies
        lobbyService.addLobby(gameId, newLobby);

        System.out.println("User " + username + " hosted game " + gameId + " with the choice " + gameChoice);

        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("gameId", gameId, "username", username, "message", "hostGameSuccess")
        );
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> joinGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String gameId   = request.get("gameId");
        String imageUrl = request.get("imageUrl");


        if (gameId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Game ID is required to join a game"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);

        if (lobby == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        boolean isUserInGame = lobby.playerExists(username) || lobby.getHost().equals(username);

        if (!isUserInGame) {
            // Uncomment if you want to prevent users from joining the game multiple times
            // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "User is already in the game"));

            lobby.addPlayer(username, imageUrl);
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("gameId", gameId, "username", username, "message", "joinGameSuccess")
        );
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/newRound")
    public ResponseEntity<Map<String, Object>> newRound(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String gameId = request.get("gameId");

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby  = lobbyService.getGameLobby(gameId);

        if (!lobby.getHost().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not the host of this lobby"));
        }

        lobbyService.removeLobbyData(gameId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "newRoundSuccess"));
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping("/close")
    public ResponseEntity<Map<String, Object>> closeLobby(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String gameId = request.get("gameId");

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);

        if (!lobby.getHost().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not the host of this lobby"));
        }

        lobbyService.removeLobby(gameId);
        // Assuming lobbyData is another data structure you're using
        lobbyService.removeLobbyData(gameId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "closeLobbySuccess"));
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/leave")
    public ResponseEntity<Map<String, Object>> leaveGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String gameId = request.get("gameId");

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);

        if (lobby.getHost().equals(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Host cannot leave the game using this endpoint"));
        }

        if (!lobby.playerExists(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "User is not in the game"));
        }

        lobby.removePlayer(username);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "leaveGameSuccess"));
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/startGame")
    public ResponseEntity<Map<String, Object>> startGame(@RequestBody Map<String, String> request, @RequestParam String gameId){
        String username = request.get("username");

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);

        if (!lobby.getHost().equals(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Non-host player cannot start the game"));
        }

        if (!lobby.playerExists(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "User is not in the game"));
        }

        String selectedCategory = "";
        if ("SpyQ".equals(lobby.getGameChoice())) {
            String category = request.get("category");
            long milliseconds = Long.parseLong(request.get("gameTimeInMS"));

            if(category.equals("HiQAreas")) {
                Random random = new Random();
                int randomIndex = random.nextInt(hiQAreas.size());
                selectedCategory = hiQAreas.get(randomIndex);
            }
            else if(category.equals("programmingLanguages")) {
                Random random = new Random();
                int randomIndex = random.nextInt(programmingLanguages.size());
                selectedCategory = programmingLanguages.get(randomIndex);
            }
            else if(category.equals("itTitles")) {
                Random random = new Random();
                int randomIndex = random.nextInt(itTitles.size());
                selectedCategory = itTitles.get(randomIndex);
            }
            else {//(category.equals("countries")) {
                String countryEndpoint = "https://restcountries.com/v3.1/region/europe";
                List<Map<String, Map<String, String>>> jsonData = restTemplate.getForObject(countryEndpoint, List.class);
                // Choose a random country
                int randomIndex = new Random().nextInt(jsonData.size());
                selectedCategory = jsonData.get(randomIndex).get("name").get("common");
            }
            // Spy game logic
            String spyName = lobby.getPlayers().get(new Random().nextInt(lobby.getPlayers().size())).getUsername();


            SpyQData.VotingObject[] votingObject = lobby.getPlayers().stream()
                    .map(player -> new SpyQData.VotingObject(player.getUsername(), 0))
                    .toArray(SpyQData.VotingObject[]::new);

            SpyQData.HasVoted[] hasVoted = lobby.getPlayers().stream()
                    .map(player -> new SpyQData.HasVoted(player.getUsername(),false))
                    .toArray(SpyQData.HasVoted[]::new);
            boolean foundSpy = false;

            // Set game end time (2 minutes by default)
            long currentTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis() + milliseconds - currentTime;

            // Set vote end time (1 minute by default)
            long endVoteTime = endTime + 30000;

            endTime = endTime / 1000;
            endVoteTime = endVoteTime / 1000;

            // Set game data
            SpyQData lobbyData = new SpyQData(spyName, selectedCategory, Arrays.asList(votingObject), Arrays.asList(hasVoted), foundSpy, endTime, endVoteTime);
            lobbyService.addLobbyData(gameId, lobbyData);
        }
        else if("HiQlash".equals(lobby.getGameChoice())){
            String category = request.get("category");
            long milliseconds = Long.parseLong(request.get("gameTimeInMS"));

            SpyQData.VotingObject[] votingObject = lobby.getPlayers().stream()
                    .map(player -> new SpyQData.VotingObject(player.getUsername(), 0))
                    .toArray(SpyQData.VotingObject[]::new);

            SpyQData.HasVoted[] hasVoted = lobby.getPlayers().stream()
                    .map(player -> new SpyQData.HasVoted(player.getUsername(),false))
                    .toArray(SpyQData.HasVoted[]::new);
            boolean foundSpy = false;

            // Set game end time (2 minutes by default)
            long currentTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis() + milliseconds - currentTime;

            // Set vote end time, equal to endTime in HiQlash
            long endVoteTime = endTime;

            endTime = endTime / 1000;
            endVoteTime = endVoteTime / 1000;

            List<String> prompts;
            if(category.equals("HiQ")) {
                prompts = promptsHiq;
            }
            else {
                prompts = promptsDefault;
            }

            HiQlashData lobbyData = new HiQlashData(lobby.getPlayers().size(), lobby.getPlayers(), Arrays.asList(votingObject), Arrays.asList(hasVoted), endTime, endVoteTime, prompts);
            lobbyService.addLobbyData(gameId, lobbyData);

            System.out.println(lobbyData);
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "startGameSuccess"));
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/spyQVote")
    public ResponseEntity<Map<String, Object>> spyQVote(@RequestBody Map<String, String> request, @RequestParam String gameId) {
        String username = request.get("username");
        String votedFor = request.get("votedFor");

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);
        if (lobby.getGameChoice().equals("SpyQ")) {
            SpyQData lobbyData = (SpyQData) lobbyService.getLobbyData(gameId);
            if(lobbyData.hasPlayerVoted((username))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Player: " + username + " already voted"));
            }

            lobbyData.setVoted(username);
            lobbyData.incrementVotes(votedFor);

            if(lobbyData.everyoneHasVoted()){
                if(lobbyData.getSpyName().equals(lobbyData.getPlayerMostVotes())) {
                    lobbyData.setFoundSpy(true);
                }
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "spyQVoteSuccessDone"));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "spyQVoteSuccess"));
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/gameData")
    public ResponseEntity<Map<String, Object>> gameDataHandle(@RequestParam String gameId, @RequestParam String username) {

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);
        String gameChoice = lobby.getGameChoice();

        if (!lobby.playerExists(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "User is not in the game"));
        }

        if(gameChoice.equals("SpyQ")) {
            SpyQData spyQData = (SpyQData) lobbyService.getLobbyData(gameId);
            if(spyQData == null) {
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "getGameDataSuccess", "data", lobby));
            }

            String country        = spyQData.getCountry();
            long endTime          = spyQData.getEndTime();
            long endVoteTime      = spyQData.getEndVoteTime();
            long endTimeConst     = spyQData.getEndTimeConst();
            long endVoteTimeConst = spyQData.getEndVoteTimeConst();
            List<SpyQData.VotingObject> votingObject = spyQData.getVotingObjectList();
            boolean foundSpy    = spyQData.foundSpy;
            String spy          = spyQData.getSpyName();

            SpyQData.GameDataMessage gameDataMessage = new SpyQData.GameDataMessage(country, endTime, endTimeConst, endVoteTime, endVoteTimeConst, gameChoice,false, "", new ArrayList<SpyQData.VotingObject>() );

            if(spyQData.everyoneHasVoted()) {
                spyQData.sortVotingObjectsByVotes();
                gameDataMessage.setVotingObject(votingObject);

            //    assert username != null;
            //    if (!username.equals(spy)) {
                gameDataMessage.setCountry(country);
                gameDataMessage.setFoundSpy(foundSpy);
                gameDataMessage.setSpyName(spy);
            //    }
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "getGameDataSuccess", "data", lobby, "gameData", gameDataMessage));
            }

            if(spy.equals(username))
                gameDataMessage.setCountry("");
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "getGameDataSuccess","data", lobby, "gameData", gameDataMessage));
        }
        else if(gameChoice.equals("HiQlash")) {

            HiQlashData hiQlashData =(HiQlashData) lobbyService.getLobbyData(gameId);
            if(hiQlashData == null) {
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "getGameDataSuccess", "data", lobby));
            }

            long endTime = hiQlashData.getEndTime();
            long endVoteTime = hiQlashData.getEndVoteTime();

            long endTimeConst     = hiQlashData.getEndTimeConst();
            long endVoteTimeConst = hiQlashData.getEndVoteTimeConst();

            List<SpyQData.VotingObject> votingObject = hiQlashData.getVotingObjectList();

            HiQlashData.GameDataMessage gameData = new HiQlashData.GameDataMessage(endTime, endTimeConst, endVoteTime, endVoteTimeConst, gameChoice, votingObject, hiQlashData.getPlayerPrompts(username));
            gameData.setHasAllAnswered(hiQlashData.hasAllPlayersAnswered());
            gameData.setHasAnswered(hiQlashData.hasPlayerAnswered(username));
            gameData.setHasVoted(hiQlashData.hasPlayerVoted(username));
            gameData.setHasAllVoted(hiQlashData.everyoneHasVoted());

            // State
            gameData.setCurrentAnswers(hiQlashData.getCurrentAnswers());
            gameData.setCurrentPrompt(hiQlashData.getCurrentPrompt());
            gameData.setCurrentPlayers(hiQlashData.getCurrentPlayers());
            gameData.setVotedForOne(hiQlashData.getVotedForOne());
            gameData.setVotedForTwo(hiQlashData.getVotedForTwo());

            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "getGameDataSuccess","data", lobby, "gameData", gameData));
        }
        System.err.println("Game Choice: " + gameChoice);
        System.err.println("Lobby: " + lobby);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "GAME MODE DOES NOT EXIST"));
    }

    @GetMapping("/getServerStatus")
    public ResponseEntity<Map<String, Object>> serverStatusHandle() {
        ConcurrentHashMap<String, GameLobby> lobbies = lobbyService.getActiveLobbies();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("active lobbies", lobbies));
    }

    @PostMapping("/hiQlashAnswer")
    public ResponseEntity<Map<String, Object>> hiQlashAnswer(@RequestBody Map<String, String> request, @RequestParam String gameId) {
        String username = request.get("username");
        String promptAnswer1 = request.get("promptAnswer1");
        String promptAnswer2 = request.get("promptAnswer2");
        List<String> answers = new ArrayList<String>(Arrays.asList(promptAnswer1, promptAnswer2));

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);
        if (lobby.getGameChoice().equals("HiQlash")) {
            HiQlashData lobbyData = (HiQlashData) lobbyService.getLobbyData(gameId);
            if(lobbyData.hasPlayerAnswered((username))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Player: " + username + " already answered"));
            }

            for(int i = 0; i < lobbyData.getPlayerPrompts(username).size(); ++i) {
                lobbyData.setAnswer(username, lobbyData.getPlayerPrompts(username).get(i), answers.get(i));
            }

            if(lobbyData.hasAllPlayersAnswered()){
                System.err.println("ALL DONE");
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "HiQlashAnswerSuccessDone"));
            }
            else
                System.err.println(lobbyData);
        }
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "HiQlashSuccess"));
    }

    @PostMapping("/hiQlashVote")
    public ResponseEntity<Map<String, Object>> hiQlashVote(@RequestBody Map<String, String> request, @RequestParam String gameId) {
        String username = request.get("username");
        String votedFor = request.get("votedFor");

        if (!lobbyService.lobbyExists(gameId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Game with ID: " + gameId + " not found"));
        }

        GameLobby lobby = lobbyService.getGameLobby(gameId);
        if (lobby.getGameChoice().equals("HiQlash")) {
            HiQlashData lobbyData = (HiQlashData) lobbyService.getLobbyData(gameId);
            if(lobbyData.hasPlayerVoted((username))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Player: " + username + " already voted"));
            }
        //    if(lobbyData.getCurrentPlayers().contains(username)) {
        //        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Player: " + username + " cannot vote on prompts which they have answered"));
        //    }

            lobbyData.setVotedFor(username, votedFor);
            lobbyData.setVoted(username);
            lobbyData.incrementVotes(votedFor);

            System.out.println("REPORTING VOTE");
            if(lobbyData.everyoneHasVoted()){
                System.out.println("DONE WITH VOTING");
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "hiQlashVoteSuccessDone"));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "hiQlashVoteSuccess"));
    }
    // Other methods and classes as needed
}

