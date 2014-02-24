package com.felixmilea.vorbit.reddit.connectivity

class RedditUser(val credential: Credential, var session: Session = null) {
  def hasValidSession(): Boolean = session != null && session.isValid
}