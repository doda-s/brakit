package com.brakit.coowner.service.exception;

public class CoOwnerNotFound extends RuntimeException {
	public CoOwnerNotFound(Long userId) {
		super("User %s is not a co-owner of this tournament.".formatted(userId));
	}
}
