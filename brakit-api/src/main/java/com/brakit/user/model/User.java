package com.brakit.user.model;

public class User {
	private Long id;
	private String name;
	private String email;
	private String passwordHash;

	public User(
		Long id,
		String name,
		String email,
		String passwordHash
	) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public static User create(String name, String email, String passwordHash) {
		return new User(null, name, normalizeEmail(email), passwordHash);
	}

	public static String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
