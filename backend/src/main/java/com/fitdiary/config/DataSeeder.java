package com.fitdiary.config;

import com.fitdiary.entity.Exercise;
import com.fitdiary.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final ExerciseRepository exerciseRepository;
    private final JdbcTemplate jdbcTemplate;

    // name → [muscleGroup, category, notes]
    private static final List<String[]> CATALOG = List.of(
        new String[]{"Squat con Bilanciere",         "Quadricipiti", "Compound",  "Schiena dritta, ginocchia in linea con i piedi. Scendi fino a parallelo o sotto. Respira e tieni il core contratto."},
        new String[]{"Front Squat",                  "Quadricipiti", "Compound",  "Bilanciere appoggiato sui deltoidi anteriori, gomiti alti. Postura più eretta rispetto allo squat classico."},
        new String[]{"Hack Squat",                   "Quadricipiti", "Compound",  "Piedi in avanti sul piano. Scendi lentamente, mantieni le ginocchia allineate."},
        new String[]{"Leg Press",                    "Quadricipiti", "Compound",  "Non bloccare mai le ginocchia in estensione. Piedi a larghezza spalle, schiena incollata allo schienale."},
        new String[]{"Bulgarian Split Squat",        "Quadricipiti", "Compound",  "Piede posteriore su una panca. Scendi verticalmente, ginocchio anteriore non oltre la punta del piede."},
        new String[]{"Affondi con Manubri",          "Quadricipiti", "Compound",  "Passo lungo, torso eretto. Ginocchio anteriore a 90°, ginocchio posteriore sfiora il suolo."},
        new String[]{"Leg Extension",                "Quadricipiti", "Isolation", "Esegui lentamente, contrai il quadricipite in cima. Evita di usare slancio."},
        new String[]{"Stacco da Terra",              "Femorali",     "Compound",  "Schiena neutra, core teso. Bilanciere vicino alle gambe per tutto il movimento. Non arrotondare la lombare."},
        new String[]{"Stacco Sumo",                  "Femorali",     "Compound",  "Piedi larghi, punte verso l'esterno. Le braccia scendono verticali dentro le gambe."},
        new String[]{"Romanian Deadlift",            "Femorali",     "Compound",  "Ginocchia leggermente flesse e fisse. Abbassa il bilanciere scorrendo lungo le gambe, senti lo stretch nei femorali."},
        new String[]{"Good Morning",                 "Femorali",     "Compound",  "Bilanciere sulle spalle, cerniera all'anca. Schiena neutra, non arrotondare. Movimento lento e controllato."},
        new String[]{"Leg Curl",                     "Femorali",     "Isolation", "Tieni i fianchi premuti sul cuscino. Porta i talloni verso i glutei, contrai e torna lentamente."},
        new String[]{"Leg Curl Sdraiato",            "Femorali",     "Isolation", "Contrai i glutei per stabilizzare il bacino. Range completo, fase eccentrica lenta."},
        new String[]{"Hip Thrust",                   "Glutei",       "Compound",  "Schiena appoggiata alla panca, bilanciere sui fianchi. Spingi con i talloni, contrai i glutei in cima. Tieni 1 secondo."},
        new String[]{"Sumo Squat",                   "Glutei",       "Compound",  "Piedi molto larghi, punte verso l'esterno. Scendi lentamente, ginocchia verso le punte dei piedi."},
        new String[]{"Glute Kickback ai Cavi",       "Glutei",       "Isolation", "Tronco stabile, non iperestendere la lombare. Porta la gamba indietro e in alto, contrai in cima."},
        new String[]{"Abductor Machine",             "Glutei",       "Isolation", "Siediti correttamente con le cosce sul supporto. Spingi verso l'esterno, torna lentamente."},
        new String[]{"Panca Piana",                  "Petto",        "Compound",  "Presa leggermente oltre la larghezza delle spalle. Arco naturale in lombare, piedi a terra. Bilanciere scende al petto inferiore."},
        new String[]{"Panca Inclinata",              "Petto",        "Compound",  "Inclinazione 30-45°. Il bilanciere scende verso la clavicola. Ottima per il petto superiore."},
        new String[]{"Panca Declinata",              "Petto",        "Compound",  "Inclinazione 15-30° verso il basso. Attiva il petto inferiore. Presa leggermente più stretta."},
        new String[]{"Push-up",                      "Petto",        "Compound",  "Corpo rigido come una tavola, core contratto. Scendi fino a sfiorare il suolo, gomiti a 45°."},
        new String[]{"Croci con Manubri",            "Petto",        "Isolation", "Leggera flessione ai gomiti fissa per tutto il movimento. Senti lo stretch in basso, contrai in cima."},
        new String[]{"Chest Fly ai Cavi",            "Petto",        "Isolation", "Cavi alti o bassi a seconda della zona da colpire. Mantieni gomiti leggermente piegati."},
        new String[]{"Cable Crossover",              "Petto",        "Isolation", "Cavi alti, incrociare le mani in basso per massimizzare la contrazione del petto inferiore."},
        new String[]{"Trazioni",                     "Dorsali",      "Compound",  "Presa prona oltre la larghezza spalle. Tira verso il petto, gomiti verso i fianchi. Non fare kipping."},
        new String[]{"Pull-up",                      "Dorsali",      "Compound",  "Presa supina (palmi verso di te). Attiva maggiormente i bicipiti rispetto alle trazioni."},
        new String[]{"Lat Machine",                  "Dorsali",      "Compound",  "Tira verso il petto, non dietro la nuca. Gomiti verso il basso e leggermente indietro."},
        new String[]{"Rematore con Bilanciere",      "Dorsali",      "Compound",  "Schiena parallela al suolo, core stabile. Tira verso l'ombelico, non verso il petto. Gomiti indietro."},
        new String[]{"Rematore con Manubrio",        "Dorsali",      "Compound",  "Ginocchio e mano libera appoggiate sulla panca. Tira il manubrio verso il fianco, gomito alto."},
        new String[]{"Pulley Basso",                 "Dorsali",      "Compound",  "Schiena eretta, non inclinarti indietro. Tira verso l'addome, gomiti indietro. Stira i dorsali in avanti."},
        new String[]{"Face Pull",                    "Dorsali",      "Isolation", "Cavo all'altezza del viso. Tira verso il viso, gomiti alti. Ottimo per i dorsali posteriori e la cuffia dei rotatori."},
        new String[]{"Straight Arm Pulldown",        "Dorsali",      "Isolation", "Braccia quasi tese, cerniera nelle spalle. Tira verso i fianchi contraendo i dorsali."},
        new String[]{"Shoulder Press con Bilanciere","Spalle",       "Compound",  "In piedi o seduto. Premi verticalmente, non spingere il bacino in avanti. Core contratto."},
        new String[]{"Shoulder Press con Manubri",   "Spalle",       "Compound",  "Seduto con schiena supportata. Porta i manubri all'altezza delle orecchie, poi premi in alto."},
        new String[]{"Arnold Press",                 "Spalle",       "Compound",  "Inizia con i palmi verso di te, ruota durante la salita. Movimento lento e controllato."},
        new String[]{"Alzate Laterali",              "Spalle",       "Isolation", "Leggera inclinazione del busto in avanti. Solleva con il gomito, non con il polso. Arriva a 90°."},
        new String[]{"Alzate Frontali",              "Spalle",       "Isolation", "Bilanciere o manubri. Solleva fino all'altezza degli occhi. Evita di oscillare il busto."},
        new String[]{"Rear Delt Fly",                "Spalle",       "Isolation", "Busto parallelo al suolo. Apri le braccia verso l'esterno, gomiti leggermente piegati. Squeeze in cima."},
        new String[]{"Upright Row",                  "Spalle",       "Compound",  "Tira verso il mento, gomiti alti. Attenzione: può stressare la spalla. Presa non troppo stretta."},
        new String[]{"Curl con Bilanciere",          "Bicipiti",     "Isolation", "Gomiti fermi ai fianchi. Arco completo, contrai in cima. Fase eccentrica lenta (3 secondi)."},
        new String[]{"Curl con Manubri",             "Bicipiti",     "Isolation", "Puoi ruotare i polsi in salita (supinazione). Alterna le braccia o fai simultaneo."},
        new String[]{"Curl a Martello",              "Bicipiti",     "Isolation", "Presa neutra (pollice in alto). Colpisce il brachiale e il brachioradiale oltre al bicipite."},
        new String[]{"Curl ai Cavi",                 "Bicipiti",     "Isolation", "Il cavo mantiene tensione costante. Ottimo come esercizio di rifinitura a fine allenamento."},
        new String[]{"Curl Concentrato",             "Bicipiti",     "Isolation", "Gomito appoggiato alla coscia. Movimento isolato, massima contrazione in cima. Tieni 1 secondo."},
        new String[]{"Curl Inclinato",               "Bicipiti",     "Isolation", "Panca inclinata a 45°. Maggiore allungamento del bicipite all'inizio del movimento."},
        new String[]{"Tricipiti Pushdown",           "Tricipiti",    "Isolation", "Gomiti fermi ai fianchi. Estendi completamente, contrai in basso. Usa corda o barra a V."},
        new String[]{"Dip alle Parallele",           "Tricipiti",    "Compound",  "Torso eretto per enfatizzare i tricipiti. Leggera inclinazione per coinvolgere anche il petto."},
        new String[]{"Skull Crusher",                "Tricipiti",    "Isolation", "Bilanciere EZ. Abbassa verso la fronte tenendo i gomiti fissi. Fase eccentrica lenta."},
        new String[]{"Close Grip Bench Press",       "Tricipiti",    "Compound",  "Presa stretta (larghezza spalle). Gomiti vicini al corpo durante la discesa del bilanciere."},
        new String[]{"Tricipiti ai Cavi",            "Tricipiti",    "Isolation", "Gomito fisso. Estendi completamente il braccio, tieni 1 secondo. Singolo braccio per maggiore focus."},
        new String[]{"French Press",                 "Tricipiti",    "Isolation", "Busto eretto, manubrio con entrambe le mani. Abbassa dietro la testa, estendi in alto."},
        new String[]{"Plank",                        "Core",         "Core",      "Corpo allineato dalla testa ai talloni. Glutei contratti, non lasciar cadere i fianchi. Respira regolarmente."},
        new String[]{"Crunch",                       "Core",         "Core",      "Non tirare il collo con le mani. Contrai l'addome, solleva solo le scapole. Movimento piccolo e controllato."},
        new String[]{"Leg Raise",                    "Core",         "Core",      "Schiena incollata al suolo, lombare non si solleva. Abbassa le gambe lentamente senza toccare terra."},
        new String[]{"Russian Twist",                "Core",         "Core",      "Schiena a 45°, gambe sollevate. Ruota il busto, non le braccia. Aggiungi un disco per progressione."},
        new String[]{"Ab Wheel",                     "Core",         "Core",      "Inizia con rullate parziali. Mantieni la lombare neutra. Non lasciar cedere i fianchi in estensione."},
        new String[]{"Hollow Body Hold",             "Core",         "Core",      "Lombare incollata al suolo, braccia e gambe tese e sollevate. Progressione: braccia avanti → gambe lontane."},
        new String[]{"Side Plank",                   "Core",         "Core",      "Corpo allineato lateralmente. Non cedere con il fianco. Aggiungi abduzione del braccio libero."},
        new String[]{"Cable Crunch",                 "Core",         "Core",      "In ginocchio, tira verso il basso con l'addome, non con le spalle. Mantieni i fianchi fermi."},
        new String[]{"Calf Raise in Piedi",          "Polpacci",     "Isolation", "Range completo: scendi fino allo stretch massimo, sali sulle punte. Tieni 1 secondo in cima."},
        new String[]{"Seated Calf Raise",            "Polpacci",     "Isolation", "Isola il soleo (muscolo sottostante il gastrocnemio). Stessa tecnica: range completo."},
        new String[]{"Donkey Calf Raise",            "Polpacci",     "Isolation", "Busto parallelo al suolo. Eccellente per il gastrocnemio. Usa una piattaforma rialzata per più range."}
    );

    @Override
    public void run(ApplicationArguments args) {
        // Aggiunge la colonna notes se non esiste (DB già inizializzato)
        jdbcTemplate.execute(
            "ALTER TABLE exercises ADD COLUMN IF NOT EXISTS notes TEXT"
        );

        Set<String> existing = exerciseRepository.findAll().stream()
                .filter(e -> !e.getIsCustom())
                .map(Exercise::getName)
                .collect(Collectors.toSet());

        // Mappa nome → note per aggiornare esercizi già presenti senza note
        Map<String, String> notesByName = CATALOG.stream()
                .filter(row -> row[3] != null)
                .collect(Collectors.toMap(row -> row[0], row -> row[3]));

        exerciseRepository.findAll().stream()
                .filter(e -> !e.getIsCustom() && e.getNotes() == null && notesByName.containsKey(e.getName()))
                .forEach(e -> {
                    e.setNotes(notesByName.get(e.getName()));
                    exerciseRepository.save(e);
                });

        // Inserisce esercizi mancanti
        CATALOG.stream()
                .filter(row -> !existing.contains(row[0]))
                .forEach(row -> exerciseRepository.save(
                        Exercise.builder()
                                .name(row[0])
                                .muscleGroup(row[1])
                                .category(row[2])
                                .notes(row[3])
                                .isCustom(false)
                                .build()
                ));
    }
}
