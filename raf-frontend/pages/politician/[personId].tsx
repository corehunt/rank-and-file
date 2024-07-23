import { GetServerSideProps } from 'next';
import Image from 'next/image';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { useState } from 'react';
import PropTypes from 'prop-types';

interface Term {
    termId: number;
    chamber: string;
    congress: number;
    district: number;
    endYr: number;
    memberType: string;
    startYr: number;
    stateCd: string;
    stateNm: string;
}

interface Politician {
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
    currentMember?: string;
    biography?: string;
    email?: string;
    imageUrl?: string;
    imgAttribution?: string;
    partyMembership?: string;
    partyStartYr?: number;
    termList?: Term[];
}

interface PoliticianPageProps {
    politician?: Politician;
    error?: string;
}

function TabPanel(props: { children?: React.ReactNode; index: any; value: any }) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box p={3}>
                    {children}
                </Box>
            )}
        </div>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.any.isRequired,
    value: PropTypes.any.isRequired,
};

function a11yProps(index: any) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

const PoliticianPage: React.FC<PoliticianPageProps> = ({ politician, error }) => {
    const [value, setValue] = useState(0);

    const handleChange = (event: React.ChangeEvent<{}>, newValue: number) => {
        setValue(newValue);
    };

    if (error) {
        return <div>Error: {error}</div>;
    }

    // Find the term with the highest congress number
    const latestTerm = politician?.termList?.slice().sort((a, b) => b.congress - a.congress)[0];

    return (
        <div className="container mx-auto p-4">
            <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                {politician?.imageUrl ? (
                    <Image
                        src={politician.imageUrl}
                        alt={politician.firstName}
                        width={200}
                        height={200}
                        onError={(e) => {
                            e.currentTarget.src = '/fallback-image.png';
                        }}
                    />
                ) : (
                    <Image src="/fallback-image.png" alt="fallback image" width={200} height={200} />
                )}
                <Typography variant="h4">{politician?.fullName}</Typography>
                <Typography variant="subtitle1" style={{ fontStyle: 'italic' }}>
                    {politician?.partyMembership} {politician?.state} {politician?.currentDistrict}
                </Typography>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-around', marginBottom: '20px' }}>
                <div>
                    <Typography variant="body1"><strong>Office:</strong> {politician?.officeLocLine1}</Typography>
                    <Typography variant="body1">{politician?.officeLocLine2}</Typography>
                    <Typography variant="body1"><strong>Phone:</strong> {politician?.phoneNo}</Typography>
                </div>
                <div>
                    <Typography variant="body1"><strong>Chamber:</strong> {latestTerm?.chamber || 'N/A'}</Typography>
                    <Typography variant="body1"><strong>Years active:</strong> Member Since {politician?.partyStartYr}</Typography>
                    <Typography variant="body1"><strong>Current Member:</strong> {politician?.currentMember}</Typography>
                </div>
            </div>
            <Box sx={{ borderBottom: 1, borderColor: 'divider', textAlign: 'center' }}>
                <Tabs value={value} onChange={handleChange} aria-label="basic tabs example" centered>
                    <Tab label="Congressional Record" {...a11yProps(0)} />
                    <Tab label="Sponsored Legislation" {...a11yProps(1)} />
                    <Tab label="Co-Sponsored Legislation" {...a11yProps(2)} />
                    <Tab label="Campaign Contributions" {...a11yProps(3)} />
                    <Tab label="Voting Record" {...a11yProps(4)} />
                </Tabs>
            </Box>
            <TabPanel value={value} index={0}>
                <Box mb={3}>
                    <Typography variant="h5" gutterBottom>Biography</Typography>
                    <Typography variant="body1" paragraph>{politician?.biography}</Typography>
                </Box>

                <Box mb={3}>
                    <Typography variant="h5" gutterBottom>Terms</Typography>
                    {politician?.termList
                        ?.slice()
                        .sort((a, b) => b.congress - a.congress)
                        .map(term => (
                            <Box key={term.termId} mb={2} p={2} border={1} borderRadius={4} borderColor="grey.300" bgcolor="grey.100">
                                <Typography variant="body1"><strong>Chamber:</strong> {term.chamber}</Typography>
                                <Typography variant="body1"><strong>Congress:</strong> {term.congress}</Typography>
                                <Typography variant="body1"><strong>District:</strong> {term.district}</Typography>
                                <Typography variant="body1"><strong>Start Year:</strong> {term.startYr}</Typography>
                                <Typography variant="body1"><strong>End Year:</strong> {term.endYr}</Typography>
                                <Typography variant="body1"><strong>State:</strong> {term.stateNm} ({term.stateCd})</Typography>
                            </Box>
                        ))}
                </Box>
            </TabPanel>
            <TabPanel value={value} index={1}>
                <Typography>Sponsored Legislation Content</Typography>
            </TabPanel>
            <TabPanel value={value} index={2}>
                <Typography>Co-Sponsored Legislation Content</Typography>
            </TabPanel>
            <TabPanel value={value} index={3}>
                <Typography>Campaign Contributions Content</Typography>
            </TabPanel>
            <TabPanel value={value} index={4}>
                <Typography>Voting Record Content</Typography>
            </TabPanel>
        </div>
    );
};

export const getServerSideProps: GetServerSideProps = async (context) => {
    try {
        const { personId } = context.params!;
        const res = await fetch(`http://localhost:3000/api/politician/${personId}`);

        if (!res.ok) {
            throw new Error(`Failed to fetch data. Status: ${res.status}`);
        }

        const politician = await res.json();

        return {
            props: {
                politician,
            },
        };
    } catch (error) {
        console.error('Error fetching data:', error);

        return {
            props: {
                error: 'Failed to fetch data',
            },
        };
    }
};

export default PoliticianPage;
