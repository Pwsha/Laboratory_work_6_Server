package org.example.init;

import java.util.Objects;

/**
 * Класс для инициализации локации
 * @author Pwsha
 * @version v1.3
 */
public class Location {
    private double x; // может быть null
    private Double y; // Не null
    private Float z; // Не null

    private Location(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
    }

    /**
     * Класс Билдер для построения конструкторов
     */
    public static class Builder {
        private double x;
        private Double y;
        private Float z;

        public Builder x(double x) {
            this.x = x;
            return this;
        }

        public Builder y(Double y) {
            this.y = y;
            return this;
        }

        public Builder z(Float z) {
            this.z = z;
            return this;
        }

        public Location build() {
            if (y == null) {
                throw new IllegalArgumentException("y не может быть null");
            }
            if (z == null) {
                throw new IllegalArgumentException("z не может быть null");
            }
            return new Location(this);
        }
    }

    public double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Float getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(x, location.x) == 0 &&
                Objects.equals(y, location.y) &&
                Objects.equals(z, location.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Location{x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}