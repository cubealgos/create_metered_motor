package metered_motor.block;

/**
 * The motor's three states and the edges between them: fed and unsignalled runs, a redstone
 * signal pauses, empty stops (docs/spec/domains/motor.md §3).
 */
public enum MotorState {
    /** No emeralds to burn: generates nothing, adds nothing to the network. */
    STOPPED,
    /** Fed and unsignalled: generates 64 rpm and adds the tier's fixed capacity (`DEC-009`). */
    RUNNING,
    /** A redstone signal, fed or not: generates nothing, adds nothing, resumes when the signal ends. */
    PAUSED
}
