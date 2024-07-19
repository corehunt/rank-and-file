'use client';

import { useState, ChangeEvent } from 'react';
import Link from 'next/link';

export interface Politician {
    personId: string;
    firstName: string;
    midName?: string;
    lastName: string;
    fullName: string;
    birthDate?: string;
    deathDate?: string;
    website?: string;
    officeLocLine1?: string;
    officeLocLine2?: string;
    phoneNo?: string;
    state?: string;
    currentDistrict?: number;
    biography?: string;
    email?: string;
    imageUrl?: string;
    imgAttribution?: string;
    partyMembership?: string;
    partyStartYr?: number;
}

const MemberSearch: React.FC = () => {
    const [politicians, setPoliticians] = useState<Politician[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [error, setError] = useState<string | null>(null);

    const fetchPoliticians = async (term: string) => {
        try {
            const response = await fetch(`/api/politician?searchTerm=${term}`);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            setPoliticians(data);
            setError(null);
        } catch (error: any) {
            console.error('Error fetching politicians:', error);
            setError(error.message || 'An unexpected error occurred');
        }
    };

    const handleSearchChange = (e: ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(e.target.value);
    };

    const handleSearchSubmit = () => {
        fetchPoliticians(searchTerm);
    };

    const filteredPoliticians = politicians.filter(politician =>
        `${politician.firstName} ${politician.lastName}`.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="w-full max-w-md mx-auto">
            <div className="flex mb-4">
                <input
                    type="text"
                    placeholder="Search politicians..."
                    value={searchTerm}
                    onChange={handleSearchChange}
                    className="p-2 border rounded-l w-full"
                />
                <button
                    onClick={handleSearchSubmit}
                    className="bg-blue-500 text-white p-2 rounded-r"
                >
                    Search
                </button>
            </div>
            {error && <div className="text-red-500">{error}</div>}
            <ul className="divide-y divide-gray-200">
                {filteredPoliticians.map(politician => (
                    <li key={politician.personId} className="py-2">
                        <Link href={`/politician/${politician.personId}`} className="text-blue-500 hover:underline">
                            {politician.firstName} {politician.lastName}
                        </Link>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default MemberSearch;
