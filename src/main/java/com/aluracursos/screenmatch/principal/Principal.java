package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=8c276616";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();

    private List<Serie> series;
    private SerieRepository repositorio;
    private Optional<Serie> serieBuscada;


    public Principal(SerieRepository repository) {
        this.repositorio= repository;
    }

    public void muestraElMenu() {

        int opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por titulos.
                    5 - Mejores series.
                    6 - Buscar series por categorias.
                    7 - Filtrar Series.
                    8 - Buscar Episodios por titulo
                    9 - Top 5 Episodios por Serie
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    top5Series();
                    break;
                case 6:
                    buscarSeriePorCategoria();
                    break;
                case 7:
                    filtrarSeriePorTemporadasYEvaluciaonI();
                    break;
                case 8:
                    filtrarPorTituloDeEpisodio();
                    break;
                case 9:
                    buscarTopFiveEpisodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }



    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }

    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Escribe el nombre de la Serie de la cual quieres ver los Episodios: ");
        var nombreSerie = teclado.nextLine();


        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        List<DatosTemporadas> temporadas = null;
        if (serie.isPresent()) {
            var serieEncontrada = serie.get();

            temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d-> d.episodios().stream()
                            .map(e-> new Episodio(d.numero(),e)))
                    .collect(Collectors.toList());
            
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }

    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        //datosSeries.add(datos);
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
     series = repositorio.findAll();

      series.stream()
              .sorted(Comparator.comparing(Serie::getGenero))
              .forEach(System.out::println);

    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escribe el titulo de la seria a buscar");
        var tituloSerie = teclado.nextLine();

         serieBuscada = repositorio.findByTituloContainsIgnoreCase(tituloSerie);

        if(serieBuscada.isPresent()){
            System.out.println("La seria buscada es: " + serieBuscada.get());
        }else{
            System.out.println("Serie no encontrada.");
        }
    }

    public void top5Series(){
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s -> System.out.println("Serie: "+ s.getTitulo() + "Evaluacion: "+ s.getEvaluacion()));
    }

    public void  buscarSeriePorCategoria(){
        System.out.println("Escribe la categoria que deseas buscar: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspañol(genero);
        List<Serie> seriePorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series de la categoria: "+ genero);
        seriePorCategoria.forEach(System.out::println);





    }


    public void filtrarSeriePorTemporadasYEvaluciaonI(){
        System.out.println("¿Filtrar séries con cuántas temporadas? ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("¿Com evaluación apartir de cuál valor? ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();

        List<Serie> filtroSeries = repositorio.seriesPorTemporadaYEvaluacion(totalTemporadas,evaluacion);


        System.out.println("*** Series filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));

    }

    public void  filtrarPorTituloDeEpisodio(){
        System.out.println("Por favor ingrese el nombre del episodio: ");
        var nombreEpisodio= teclado.nextLine();
        List<Episodio> episodiosEncontrados= repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie : %s Temporada %s Episodio %s Evalucion %s\n",e.getSerie().getTitulo(),e.getTemporada(),e.getNumeroEpisodio(),e.getEvaluacion()));

    }

    private void buscarTopFiveEpisodios(){
        buscarSeriePorTitulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.topFiveMejores(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s - Temporada %s - Episodio %s - Evaluación %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()));

        }

    }

}

