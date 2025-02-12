"use client";

import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";

interface BillFiltersProps {
  filters: {
    chamber: string[];
    party: string[];
    congress: string[];
  };
  onFilterChange: (type: string, value: string | string[]) => void;
}

export default function BillFilters({ filters, onFilterChange }: BillFiltersProps) {
  const congresses = [
    "118th Congress (2023-2024)",
    "117th Congress (2021-2022)",
    "116th Congress (2019-2020)",
    "115th Congress (2017-2018)"
  ];

  return (
      <Card className="sticky top-4">
        <CardHeader>
          <h2 className="text-lg font-semibold">Filters</h2>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Congress Filter */}
          {/*<div className="space-y-4">*/}
          {/*  <h3 className="font-medium">Congress</h3>*/}
          {/*  <div className="space-y-2">*/}
          {/*    {congresses.map((congress) => (*/}
          {/*        <div key={congress} className="flex items-center space-x-2">*/}
          {/*          <Checkbox*/}
          {/*              id={congress}*/}
          {/*              checked={filters.congress.includes(congress)}*/}
          {/*              onCheckedChange={(checked) => {*/}
          {/*                const newList = checked*/}
          {/*                    ? [...filters.congress, congress]*/}
          {/*                    : filters.congress.filter((c) => c !== congress);*/}
          {/*                onFilterChange("congress", newList);*/}
          {/*              }}*/}
          {/*          />*/}
          {/*          <Label htmlFor={congress}>{congress}</Label>*/}
          {/*        </div>*/}
          {/*    ))}*/}
          {/*  </div>*/}
          {/*</div>*/}

          {/*<Separator />*/}

          {/* Chamber Filter (House / Senate) */}
          <div className="space-y-4">
            <h3 className="font-medium">Chamber</h3>
            <div className="space-y-2">
              <div className="flex items-center space-x-2">
                <Checkbox
                    id="house"
                    checked={filters.chamber.includes("house")}
                    onCheckedChange={(checked) => {
                      const newChambers = checked
                          ? [...filters.chamber, "house"]
                          : filters.chamber.filter((c) => c !== "house");
                      onFilterChange("chamber", newChambers);
                    }}
                />
                <Label htmlFor="house">House Bills</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox
                    id="senate"
                    checked={filters.chamber.includes("senate")}
                    onCheckedChange={(checked) => {
                      const newChambers = checked
                          ? [...filters.chamber, "senate"]
                          : filters.chamber.filter((c) => c !== "senate");
                      onFilterChange("chamber", newChambers);
                    }}
                />
                <Label htmlFor="senate">Senate Bills</Label>
              </div>
            </div>
          </div>

          <Separator />

          {/* Party Filter */}
          <div className="space-y-4">
            <h3 className="font-medium">Party</h3>
            <div className="space-y-2">
              <div className="flex items-center space-x-2">
                <Checkbox
                    id="democratic"
                    checked={filters.party.includes("Democratic")}
                    onCheckedChange={(checked) => {
                      const newParty = checked
                          ? [...filters.party, "Democratic"]
                          : filters.party.filter((p) => p !== "Democratic");
                      onFilterChange("party", newParty);
                    }}
                />
                <Label htmlFor="democratic">Democratic Sponsor</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox
                    id="republican"
                    checked={filters.party.includes("Republican")}
                    onCheckedChange={(checked) => {
                      const newParty = checked
                          ? [...filters.party, "Republican"]
                          : filters.party.filter((p) => p !== "Republican");
                      onFilterChange("party", newParty);
                    }}
                />
                <Label htmlFor="republican">Republican Sponsor</Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox
                    id="bipartisan"
                    checked={filters.party.includes("Bipartisan")}
                    onCheckedChange={(checked) => {
                      const newParty = checked
                          ? [...filters.party, "Bipartisan"]
                          : filters.party.filter((p) => p !== "Bipartisan");
                      onFilterChange("party", newParty);
                    }}
                />
                <Label htmlFor="bipartisan">Bipartisan</Label>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
  );
}