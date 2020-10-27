package edu.uclm.esi.videochat.classicdao;

import java.util.Vector;

public class Broker {
	private int numeroDeConexiones = 10;
	private Vector<BD> libres;
	private Vector<BD> ocupadas;
	
	private Broker() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			this.libres = new Vector<>();
			this.ocupadas = new Vector<>();
			for (int i=0; i<numeroDeConexiones; i++)
				this.libres.add(new BD(this));
		} catch (Exception e) { 
			System.out.println(e.toString());
		}
	}
	
	private static class BrokerHolder {
		static Broker singleton=new Broker();
	}
	
	public static Broker get() {
		return BrokerHolder.singleton;
	}
	
	public void close(BD bd) {
		this.ocupadas.remove(bd);
		this.libres.add(bd);
	}

	public BD getBD() {
		return this.libres.get(0);
	}
}
