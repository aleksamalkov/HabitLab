# TODO

## MainActivity
- [x] pravljenje nove navike tako da korisnik moze da izabere da li je numericka ili binarna
- [x] unosenje cilja za numericke (nije potvrdjeno da radi)
- [x] ispisivanje datuma i/ili dana u nedelji
- [x] cuvanje izvrsavanja navika u bazi
- [ ] xuvanje izvrsavanja numerickih navika u bazi
- [x] skrolovanje dugacke liste navika
- [x] text field za brojeve
- [x] pokretanje drugog activity-a kada se klikne naziv neke navike
- [ ] slanje habit objekta activity-u koji se pokrece (za ovo je mozda potrebno promeniti klasu Habit tako da implementira neki interfejs nmp)

## drugi activity
drugi activity koji ce prikazivati statistike za neku konkretnu naviku
- [ ] napraviti klasu za activity
- [ ] napraviti klasu koja nasledjuje ViewModel u kojoj ce se cuvati stanje (slicno kao StateHolder za MainActivity)
- [ ] naziv navike i cilj ako je numericka
  (za ovo je potrebno da se prilikom prebacivanja activity-a prenesu podaci o navici, za pocetak moze i samo da pise neki placeholder string)
- [ ] kalendar
- [ ] pie chart
- [ ] histogram (za numericku)
- [ ] streak (trenutni i/ili najbolji)
- [ ] dugme za vracanje na MainActivity (pozivom funkcije finish())
- [ ] dugme za brisanje navike