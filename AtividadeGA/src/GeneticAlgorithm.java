import java.util.*;
import java.util.stream.Collectors;

class Chromosome implements Comparable<Chromosome> {
    private String route;
    private int penalty;

    public Chromosome(String route) {
        this.route = route;
        this.penalty = calculatePenalty();
    }

    private int calculatePenalty() {
        int penalty = 0;

        // 1. Penalty for larger city before smaller
        for (int i = 0; i < route.length(); i++) {
            for (int j = i + 1; j < route.length(); j++) {
                if (Character.getNumericValue(route.charAt(i)) > Character.getNumericValue(route.charAt(j))) {
                    penalty += 10;
                }
            }
        }

        // 2. Penalty for repeated cities
        Set<Character> uniqueCities = new HashSet<>();
        for (char c : route.toCharArray()) {
            uniqueCities.add(c);
        }
        if (uniqueCities.size() != route.length()) {
            penalty += 20 * (route.length() - uniqueCities.size());
        }

        return penalty;
    }

    public String getRoute() {
        return route;
    }

    public int getPenalty() {
        return penalty;
    }

    @Override
    public int compareTo(Chromosome other) {
        return Integer.compare(this.penalty, other.penalty);
    }

    @Override
    public String toString() {
        return route + " (Penalty: " + penalty + ")";
    }
}

public class GeneticAlgorithm {
    private static Random random = new Random();

    private static String generateRandomRoute() {
        List<Character> cities = new ArrayList<>();
        for (char c = '1'; c <= '9'; c++) {
            cities.add(c);
        }
        Collections.shuffle(cities);
        return cities.stream().map(String::valueOf).collect(Collectors.joining());
    }

    private static void displayPopulation(List<Chromosome> population, int generation) {
        System.out.println("\n=== Generation " + generation + " ===");
        for (Chromosome chromosome : population) {
            System.out.println(chromosome);
            try {
                Thread.sleep(50);  // Small delay between individuals
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static Chromosome tournamentSelection(List<Chromosome> population) {
        List<Chromosome> tournament = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tournament.add(population.get(random.nextInt(population.size())));
        }
        return Collections.min(tournament);
    }

    private static Chromosome crossover(Chromosome parent1, Chromosome parent2) {
        String p1 = parent1.getRoute();
        String p2 = parent2.getRoute();

        int cut1 = random.nextInt(9);
        int cut2 = random.nextInt(9);
        if (cut1 > cut2) {
            int temp = cut1;
            cut1 = cut2;
            cut2 = temp;
        }

        char[] child = new char[9];
        Arrays.fill(child, ' ');

        // Copy the segment between cut points from parent1
        for (int i = cut1; i < cut2; i++) {
            child[i] = p1.charAt(i);
        }

        // Fill remaining positions with cities from parent2 in order
        int p2Pos = 0;
        for (int i = 0; i < 9; i++) {
            if (child[i] == ' ') {
                while (containsCity(child, p2.charAt(p2Pos))) {
                    p2Pos++;
                }
                child[i] = p2.charAt(p2Pos);
            }
        }

        return new Chromosome(new String(child));
    }

    private static boolean containsCity(char[] route, char city) {
        for (char c : route) {
            if (c == city) {
                return true;
            }
        }
        return false;
    }

    private static Chromosome mutate(Chromosome chromosome, int mutationRate) {
        if (random.nextInt(100) >= mutationRate) {
            return chromosome;
        }

        char[] route = chromosome.getRoute().toCharArray();
        int idx1 = random.nextInt(9);
        int idx2 = random.nextInt(9);

        char temp = route[idx1];
        route[idx1] = route[idx2];
        route[idx2] = temp;

        return new Chromosome(new String(route));
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Genetic Algorithm for City Route Optimization ===");
        System.out.println("Objective: Find the route 1-2-3-4-5-6-7-8-9 with penalty 0\n");

        // Configuration
        System.out.print("Population size: ");
        int populationSize = scanner.nextInt();

        System.out.print("Mutation rate (%): ");
        int mutationRate = scanner.nextInt();

        System.out.print("Maximum generations: ");
        int maxGenerations = scanner.nextInt();

        // Initial population
        List<Chromosome> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(new Chromosome(generateRandomRoute()));
        }
        Collections.sort(population);

        displayPopulation(population, 1);

        int generation;
        for (generation = 1; generation < maxGenerations; generation++) {
            List<Chromosome> newPopulation = new ArrayList<>();

            // Elitism (keep the best)
            newPopulation.add(population.get(0));

            // Tournament selection
            while (newPopulation.size() < populationSize) {
                newPopulation.add(tournamentSelection(population));
            }

            // Crossover
            List<Chromosome> offspring = new ArrayList<>();
            for (int i = 0; i < populationSize / 2; i++) {
                Chromosome parent1 = newPopulation.get(random.nextInt(newPopulation.size()));
                Chromosome parent2 = newPopulation.get(random.nextInt(newPopulation.size()));

                Chromosome child1 = crossover(parent1, parent2);
                Chromosome child2 = crossover(parent2, parent1);

                offspring.add(child1);
                offspring.add(child2);
            }

            // Mutation
            for (int i = 0; i < offspring.size(); i++) {
                offspring.set(i, mutate(offspring.get(i), mutationRate));
            }

            // New population
            population = new ArrayList<>(offspring);
            Collections.sort(population);

            displayPopulation(population, generation + 1);

            // Stop condition
            if (population.get(0).getPenalty() == 0) {
                System.out.println("\n✅ Perfect solution found!");
                break;
            }
        }

        System.out.println("\n=== Final Result ===");
        System.out.println("Best route found: " + population.get(0));
        System.out.println("Total generations: " + (generation + 1));

        scanner.close();
    }
}