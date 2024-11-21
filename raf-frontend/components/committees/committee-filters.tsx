"use client";

import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";

interface CommitteeFiltersProps {
  filters: {
    chamber: string;
    type: string[];
    policyAreas: string[];
  };
  onFilterChange: (type: string, value: string | string[]) => void;
}

export default function CommitteeFilters({ filters, onFilterChange }: CommitteeFiltersProps) {
  return (
    <Card className="sticky top-4">
      <CardHeader>
        <h2 className="text-lg font-semibold">Filters</h2>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-4">
          <h3 className="font-medium">Chamber</h3>
          <RadioGroup 
            value={filters.chamber}
            onValueChange={(value) => onFilterChange("chamber", value)}
          >
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="all" id="all" />
              <Label htmlFor="all">All Chambers</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="house" id="house" />
              <Label htmlFor="house">House</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="senate" id="senate" />
              <Label htmlFor="senate">Senate</Label>
            </div>
            <div className="flex items-center space-x-2">
              <RadioGroupItem value="joint" id="joint" />
              <Label htmlFor="joint">Joint Committees</Label>
            </div>
          </RadioGroup>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Committee Type</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="standing"
                checked={filters.type.includes("standing")}
                onCheckedChange={(checked) => {
                  const newTypes = checked
                    ? [...filters.type, "standing"]
                    : filters.type.filter(t => t !== "standing");
                  onFilterChange("type", newTypes);
                }}
              />
              <Label htmlFor="standing">Standing Committees</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="select"
                checked={filters.type.includes("select")}
                onCheckedChange={(checked) => {
                  const newTypes = checked
                    ? [...filters.type, "select"]
                    : filters.type.filter(t => t !== "select");
                  onFilterChange("type", newTypes);
                }}
              />
              <Label htmlFor="select">Select Committees</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="subcommittees"
                checked={filters.type.includes("subcommittees")}
                onCheckedChange={(checked) => {
                  const newTypes = checked
                    ? [...filters.type, "subcommittees"]
                    : filters.type.filter(t => t !== "subcommittees");
                  onFilterChange("type", newTypes);
                }}
              />
              <Label htmlFor="subcommittees">Subcommittees</Label>
            </div>
          </div>
        </div>

        <Separator />

        <div className="space-y-4">
          <h3 className="font-medium">Policy Areas</h3>
          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="finance"
                checked={filters.policyAreas.includes("finance")}
                onCheckedChange={(checked) => {
                  const newAreas = checked
                    ? [...filters.policyAreas, "finance"]
                    : filters.policyAreas.filter(a => a !== "finance");
                  onFilterChange("policyAreas", newAreas);
                }}
              />
              <Label htmlFor="finance">Finance & Budget</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="foreign"
                checked={filters.policyAreas.includes("foreign")}
                onCheckedChange={(checked) => {
                  const newAreas = checked
                    ? [...filters.policyAreas, "foreign"]
                    : filters.policyAreas.filter(a => a !== "foreign");
                  onFilterChange("policyAreas", newAreas);
                }}
              />
              <Label htmlFor="foreign">Foreign Affairs</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="security"
                checked={filters.policyAreas.includes("security")}
                onCheckedChange={(checked) => {
                  const newAreas = checked
                    ? [...filters.policyAreas, "security"]
                    : filters.policyAreas.filter(a => a !== "security");
                  onFilterChange("policyAreas", newAreas);
                }}
              />
              <Label htmlFor="security">Security & Defense</Label>
            </div>
            <div className="flex items-center space-x-2">
              <Checkbox 
                id="energy"
                checked={filters.policyAreas.includes("energy")}
                onCheckedChange={(checked) => {
                  const newAreas = checked
                    ? [...filters.policyAreas, "energy"]
                    : filters.policyAreas.filter(a => a !== "energy");
                  onFilterChange("policyAreas", newAreas);
                }}
              />
              <Label htmlFor="energy">Energy & Environment</Label>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}