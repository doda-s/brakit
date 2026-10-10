package com.brakit.member.service.exception;

public class MemberNotFound extends RuntimeException {
	public MemberNotFound(Long id) {
		super("No member with id %s was found.".formatted(id));
	}
}
