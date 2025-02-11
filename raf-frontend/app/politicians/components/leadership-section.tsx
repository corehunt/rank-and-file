import { LeadershipCard } from "./leadership-card";

export interface LeadershipDTO {
    leadershipId: string;
    leadershipType: string;
    currentLeader?: string | boolean;
    person: {
        personId: string;
        fullName: string | null;
        partyMembership: string | null;
        currentDistrict: number | null;
        state: string | null;
        imageUrl: string | null;
    };
}


interface LeadershipSectionProps {
    title: string;
    leaders: LeadershipDTO[];
}

export function LeadershipSection({ title, leaders }: LeadershipSectionProps) {
    return (
        <div className="space-y-4">
            <h2 className="text-xl font-semibold">{title}</h2>
            <div className="grid gap-4 sm:grid-cols-2">
                {leaders.map((dto) => (
                    <LeadershipCard
                        key={dto.leadershipId}
                        leadershipId={dto.leadershipId}
                        leadershipType={dto.leadershipType}
                        currentLeader={dto.currentLeader !== undefined ? dto.currentLeader : false}
                        person={dto.person}
                    />
                ))}
            </div>
        </div>
    );
}
