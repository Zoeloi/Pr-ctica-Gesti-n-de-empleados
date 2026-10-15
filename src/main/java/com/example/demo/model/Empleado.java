package com.example.demo.model;

public class Empleado {
    private int idEmpleado; // Necesario para las operaciones de actualización y eliminación
    private String codigo;
    private String nombre;
    private String correo;
    private String cargo;
    private double salario;

    public Empleado(int idEmpleado, String codigo, String nombre, String correo, String cargo, double salario) {
        this.idEmpleado = idEmpleado;
        this.codigo = codigo;
        this.nombre = nombre;
        this.correo = correo;
        this.cargo = cargo;
        this.salario = salario;
    }

    // Getters
    public int getIdEmpleado() { return idEmpleado; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getCargo() { return cargo; }
    public double getSalario() { return salario; }

    // Setters
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setSalario(double salario) { this.salario = salario; }
}